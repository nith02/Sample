from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import tensorflow as tf
from tensorflow.python.keras import backend
from tensorflow.python.keras import initializers
from tensorflow.python.keras import models
from tensorflow.python.keras import regularizers
from official.vision.image_classification.resnet import imagenet_preprocessing

import math
import os
from typing import Any, Dict, Optional, Text, Tuple

from absl import logging
from dataclasses import dataclass
import tensorflow as tf

from official.modeling import tf_utils
from official.modeling.hyperparams import base_config
from official.vision.image_classification import preprocessing
from official.vision.image_classification.efficientnet import common_modules

layers = tf.keras.layers


@dataclass
class BlockConfig(base_config.Config):
  """Config for a single MB Conv Block."""
  input_filters: int = 0
  output_filters: int = 0
  kernel_size: int = 3
  num_repeat: int = 1
  expand_ratio: int = 1
  strides: Tuple[int, int] = (1, 1)
  se_ratio: Optional[float] = None
  id_skip: bool = True
  fused_conv: bool = False
  conv_type: str = 'depthwise'


@dataclass
class ModelConfig(base_config.Config):
  """Default Config for Efficientnet-B0."""
  width_coefficient: float = 1.0
  depth_coefficient: float = 1.0
  resolution: int = 224
  dropout_rate: float = 0.2
  blocks: Tuple[BlockConfig, ...] = (
      # (input_filters, output_filters, kernel_size, num_repeat,
      #  expand_ratio, strides, se_ratio)
      # pylint: disable=bad-whitespace
      BlockConfig.from_args(32,  16,  3, 1, 1, (1, 1), 0.25),
      BlockConfig.from_args(16,  24,  3, 2, 6, (2, 2), 0.25),
      BlockConfig.from_args(24,  40,  5, 2, 6, (2, 2), 0.25),
      BlockConfig.from_args(40,  80,  3, 3, 6, (2, 2), 0.25),
      BlockConfig.from_args(80,  112, 5, 3, 6, (1, 1), 0.25),
      BlockConfig.from_args(112, 192, 5, 4, 6, (2, 2), 0.25),
      BlockConfig.from_args(192, 320, 3, 1, 6, (1, 1), 0.25),
      # pylint: enable=bad-whitespace
  )
  stem_base_filters: int = 32
  top_base_filters: int = 1280
  activation: str = 'simple_swish'
  batch_norm: str = 'default'
  bn_momentum: float = 0.99
  bn_epsilon: float = 1e-3
  # While the original implementation used a weight decay of 1e-5,
  # tf.nn.l2_loss divides it by 2, so we halve this to compensate in Keras
  weight_decay: float = 5e-6
  drop_connect_rate: float = 0.2
  depth_divisor: int = 8
  min_depth: Optional[int] = None
  use_se: bool = True
  input_channels: int = 3
  num_classes: int = 1000
  model_name: str = 'efficientnet'
  rescale_input: bool = True
  data_format: str = 'channels_last'
  dtype: str = 'float32'


MODEL_CONFIGS = {
    # (width, depth, resolution, dropout)
    'efficientnet-b0': ModelConfig.from_args(1.0, 1.0, 224, 0.2),
    'efficientnet-b1': ModelConfig.from_args(1.0, 1.1, 240, 0.2),
    'efficientnet-b2': ModelConfig.from_args(1.1, 1.2, 260, 0.3),
    'efficientnet-b3': ModelConfig.from_args(1.2, 1.4, 300, 0.3),
    'efficientnet-b4': ModelConfig.from_args(1.4, 1.8, 380, 0.4),
    'efficientnet-b5': ModelConfig.from_args(1.6, 2.2, 456, 0.4),
    'efficientnet-b6': ModelConfig.from_args(1.8, 2.6, 528, 0.5),
    'efficientnet-b7': ModelConfig.from_args(2.0, 3.1, 600, 0.5),
    'efficientnet-b8': ModelConfig.from_args(2.2, 3.6, 672, 0.5),
    'efficientnet-l2': ModelConfig.from_args(4.3, 5.3, 800, 0.5),
}

CONV_KERNEL_INITIALIZER = {
    'class_name': 'VarianceScaling',
    'config': {
        'scale': 2.0,
        'mode': 'fan_out',
        # Note: this is a truncated normal distribution
        'distribution': 'normal'
    }
}

DENSE_KERNEL_INITIALIZER = {
    'class_name': 'VarianceScaling',
    'config': {
        'scale': 1 / 3.0,
        'mode': 'fan_out',
        'distribution': 'uniform'
    }
}


def round_filters(filters: int,
                  config: ModelConfig) -> int:
  """Round number of filters based on width coefficient."""
  width_coefficient = config.width_coefficient
  min_depth = config.min_depth
  divisor = config.depth_divisor
  orig_filters = filters

  if not width_coefficient:
    return filters

  filters *= width_coefficient
  min_depth = min_depth or divisor
  new_filters = max(min_depth, int(filters + divisor / 2) // divisor * divisor)
  # Make sure that round down does not go down by more than 10%.
  if new_filters < 0.9 * filters:
    new_filters += divisor
  logging.info('round_filter input=%s output=%s', orig_filters, new_filters)
  return int(new_filters)


def round_repeats(repeats: int, depth_coefficient: float) -> int:
  """Round number of repeats based on depth coefficient."""
  return int(math.ceil(depth_coefficient * repeats))


def conv2d_block(inputs: tf.Tensor,
                 conv_filters: Optional[int],
                 config: ModelConfig,
                 kernel_size: Any = (1, 1),
                 strides: Any = (1, 1),
                 use_batch_norm: bool = True,
                 use_bias: bool = False,
                 activation: Any = None,
                 depthwise: bool = False,
                 name: Text = None):
  """A conv2d followed by batch norm and an activation."""
  batch_norm = common_modules.get_batch_norm(config.batch_norm)
  bn_momentum = config.bn_momentum
  bn_epsilon = config.bn_epsilon
  data_format = tf.keras.backend.image_data_format()
  weight_decay = config.weight_decay

  name = name or ''

  # Collect args based on what kind of conv2d block is desired
  init_kwargs = {
      'kernel_size': kernel_size,
      'strides': strides,
      'use_bias': use_bias,
      'padding': 'same',
      'name': name + '_conv2d',
      'kernel_regularizer': tf.keras.regularizers.l2(weight_decay),
      'bias_regularizer': tf.keras.regularizers.l2(weight_decay),
  }

  if depthwise:
    conv2d = tf.keras.layers.DepthwiseConv2D
    init_kwargs.update({'depthwise_initializer': CONV_KERNEL_INITIALIZER})
  else:
    conv2d = tf.keras.layers.Conv2D
    init_kwargs.update({'filters': conv_filters,
                        'kernel_initializer': CONV_KERNEL_INITIALIZER})

  x = conv2d(**init_kwargs)(inputs)

  if use_batch_norm:
    bn_axis = 1 if data_format == 'channels_first' else -1
    x = batch_norm(axis=bn_axis,
                   momentum=bn_momentum,
                   epsilon=bn_epsilon,
                   name=name + '_bn')(x)

  if activation is not None:
    x = tf.keras.layers.Activation(activation,
                                   name=name + '_activation')(x)
  return x


def mb_conv_block(inputs: tf.Tensor,
                  block: BlockConfig,
                  config: ModelConfig,
                  prefix: Text = None):
  """Mobile Inverted Residual Bottleneck.

  Args:
    inputs: the Keras input to the block
    block: BlockConfig, arguments to create a Block
    config: ModelConfig, a set of model parameters
    prefix: prefix for naming all layers

  Returns:
    the output of the block
  """
  use_se = config.use_se
  activation = tf_utils.get_activation(config.activation)
  drop_connect_rate = config.drop_connect_rate
  data_format = tf.keras.backend.image_data_format()
  use_depthwise = block.conv_type != 'no_depthwise'
  prefix = prefix or ''

  filters = block.input_filters * block.expand_ratio

  x = inputs

  if block.fused_conv:
    # If we use fused mbconv, skip expansion and use regular conv.
    x = conv2d_block(x,
                     filters,
                     config,
                     kernel_size=block.kernel_size,
                     strides=block.strides,
                     activation=activation,
                     name=prefix + 'fused')
  else:
    if block.expand_ratio != 1:
      # Expansion phase
      kernel_size = (1, 1) if use_depthwise else (3, 3)
      x = conv2d_block(x,
                       filters,
                       config,
                       kernel_size=kernel_size,
                       activation=activation,
                       name=prefix + 'expand')

    # Depthwise Convolution
    if use_depthwise:
      x = conv2d_block(x,
                       conv_filters=None,
                       config=config,
                       kernel_size=block.kernel_size,
                       strides=block.strides,
                       activation=activation,
                       depthwise=True,
                       name=prefix + 'depthwise')

  # Squeeze and Excitation phase
  if use_se:
    assert block.se_ratio is not None
    assert 0 < block.se_ratio <= 1
    num_reduced_filters = max(1, int(
        block.input_filters * block.se_ratio
    ))

    if data_format == 'channels_first':
      se_shape = (filters, 1, 1)
    else:
      se_shape = (1, 1, filters)

    se = tf.keras.layers.GlobalAveragePooling2D(name=prefix + 'se_squeeze')(x)
    se = tf.keras.layers.Reshape(se_shape, name=prefix + 'se_reshape')(se)

    se = conv2d_block(se,
                      num_reduced_filters,
                      config,
                      use_bias=True,
                      use_batch_norm=False,
                      activation=activation,
                      name=prefix + 'se_reduce')
    se = conv2d_block(se,
                      filters,
                      config,
                      use_bias=True,
                      use_batch_norm=False,
                      activation='sigmoid',
                      name=prefix + 'se_expand')
    x = tf.keras.layers.multiply([x, se], name=prefix + 'se_excite')

  # Output phase
  x = conv2d_block(x,
                   block.output_filters,
                   config,
                   activation=None,
                   name=prefix + 'project')

  # Add identity so that quantization-aware training can insert quantization
  # ops correctly.
  x = tf.keras.layers.Activation(tf_utils.get_activation('identity'),
                                 name=prefix + 'id')(x)

  if (block.id_skip
      and all(s == 1 for s in block.strides)
      and block.input_filters == block.output_filters):
    if drop_connect_rate and drop_connect_rate > 0:
      # Apply dropconnect
      # The only difference between dropout and dropconnect in TF is scaling by
      # drop_connect_rate during training. See:
      # https://github.com/keras-team/keras/pull/9898#issuecomment-380577612
      x = tf.keras.layers.Dropout(drop_connect_rate,
                                  noise_shape=(None, 1, 1, 1),
                                  name=prefix + 'drop')(x)

    x = tf.keras.layers.add([x, inputs], name=prefix + 'add')

  return x


def efficientnet(image_input: tf.keras.layers.Input,
                 config: ModelConfig):
  """Creates an EfficientNet graph given the model parameters.

  This function is wrapped by the `EfficientNet` class to make a tf.keras.Model.

  Args:
    image_input: the input batch of images
    config: the model config

  Returns:
    the output of efficientnet
  """
  depth_coefficient = config.depth_coefficient
  blocks = config.blocks
  stem_base_filters = config.stem_base_filters
  top_base_filters = config.top_base_filters
  activation = tf_utils.get_activation(config.activation)
  dropout_rate = config.dropout_rate
  drop_connect_rate = config.drop_connect_rate
  num_classes = config.num_classes
  input_channels = config.input_channels
  rescale_input = config.rescale_input
  data_format = tf.keras.backend.image_data_format()
  dtype = config.dtype
  weight_decay = config.weight_decay

  x = image_input
  if data_format == 'channels_first':
    # Happens on GPU/TPU if available.
    x = tf.keras.layers.Permute((3, 1, 2))(x)
  if rescale_input:
    x = preprocessing.normalize_images(x,
                                       num_channels=input_channels,
                                       dtype=dtype,
                                       data_format=data_format)

  # Build stem
  x = conv2d_block(x,
                   round_filters(stem_base_filters, config),
                   config,
                   kernel_size=[3, 3],
                   strides=[2, 2],
                   activation=activation,
                   name='stem')

  # Build blocks
  num_blocks_total = sum(
      round_repeats(block.num_repeat, depth_coefficient) for block in blocks)
  block_num = 0

  for stack_idx, block in enumerate(blocks):
    assert block.num_repeat > 0
    # Update block input and output filters based on depth multiplier
    block = block.replace(
        input_filters=round_filters(block.input_filters, config),
        output_filters=round_filters(block.output_filters, config),
        num_repeat=round_repeats(block.num_repeat, depth_coefficient))

    # The first block needs to take care of stride and filter size increase
    drop_rate = drop_connect_rate * float(block_num) / num_blocks_total
    config = config.replace(drop_connect_rate=drop_rate)
    block_prefix = 'stack_{}/block_0/'.format(stack_idx)
    x = mb_conv_block(x, block, config, block_prefix)
    block_num += 1
    if block.num_repeat > 1:
      block = block.replace(
          input_filters=block.output_filters,
          strides=[1, 1]
      )

      for block_idx in range(block.num_repeat - 1):
        drop_rate = drop_connect_rate * float(block_num) / num_blocks_total
        config = config.replace(drop_connect_rate=drop_rate)
        block_prefix = 'stack_{}/block_{}/'.format(stack_idx, block_idx + 1)
        x = mb_conv_block(x, block, config, prefix=block_prefix)
        block_num += 1

  # Build top
  x = conv2d_block(x,
                   round_filters(top_base_filters, config),
                   config,
                   activation=activation,
                   name='top')

  # Build classifier
  x = tf.keras.layers.GlobalAveragePooling2D(name='top_pool')(x)
  if dropout_rate and dropout_rate > 0:
    x = tf.keras.layers.Dropout(dropout_rate, name='top_dropout')(x)
  x = tf.keras.layers.Dense(
      num_classes,
      kernel_initializer=DENSE_KERNEL_INITIALIZER,
      kernel_regularizer=tf.keras.regularizers.l2(weight_decay),
      bias_regularizer=tf.keras.regularizers.l2(weight_decay),
      name='logits')(x)
  x = tf.keras.layers.Activation('softmax', name='probs')(x)

  return x


@tf.keras.utils.register_keras_serializable(package='Vision')
class EfficientNet(tf.keras.Model):
  """Wrapper class for an EfficientNet Keras model.

  Contains helper methods to build, manage, and save metadata about the model.
  """

  def __init__(self,
               config: ModelConfig = None,
               overrides: Dict[Text, Any] = None):
    """Create an EfficientNet model.

    Args:
      config: (optional) the main model parameters to create the model
      overrides: (optional) a dict containing keys that can override
                 config
    """
    overrides = overrides or {}
    config = config or ModelConfig()

    self.config = config.replace(**overrides)

    input_channels = self.config.input_channels
    model_name = self.config.model_name
    input_shape = (None, None, input_channels)  # Should handle any size image
    image_input = tf.keras.layers.Input(shape=input_shape)

    output = efficientnet(image_input, self.config)

    # Cast to float32 in case we have a different model dtype
    output = tf.cast(output, tf.float32)

    logging.info('Building model %s with params %s',
                 model_name,
                 self.config)

    super(EfficientNet, self).__init__(
        inputs=image_input, outputs=output, name=model_name)

  @classmethod
  def from_name(cls,
                model_name: Text,
                model_weights_path: Text = None,
                weights_format: Text = 'saved_model',
                overrides: Dict[Text, Any] = None):
    """Construct an EfficientNet model from a predefined model name.

    E.g., `EfficientNet.from_name('efficientnet-b0')`.

    Args:
      model_name: the predefined model name
      model_weights_path: the path to the weights (h5 file or saved model dir)
      weights_format: the model weights format. One of 'saved_model', 'h5',
       or 'checkpoint'.
      overrides: (optional) a dict containing keys that can override config

    Returns:
      A constructed EfficientNet instance.
    """
    model_configs = dict(MODEL_CONFIGS)
    overrides = dict(overrides) if overrides else {}

    # One can define their own custom models if necessary
    model_configs.update(overrides.pop('model_config', {}))

    if model_name not in model_configs:
      raise ValueError('Unknown model name {}'.format(model_name))

    config = model_configs[model_name]

    model = cls(config=config, overrides=overrides)

    if model_weights_path:
      common_modules.load_weights(model,
                                  model_weights_path,
                                  weights_format=weights_format)

    return model



def _gen_l2_regularizer(use_l2_regularizer=True, l2_weight_decay=1e-4):
  return regularizers.l2(l2_weight_decay) if use_l2_regularizer else None


def identity_block(input_tensor,
                   kernel_size,
                   filters,
                   stage,
                   block,
                   use_l2_regularizer=True,
                   batch_norm_decay=0.9,
                   batch_norm_epsilon=1e-5):
  """The identity block is the block that has no conv layer at shortcut.

  Args:
    input_tensor: input tensor
    kernel_size: default 3, the kernel size of middle conv layer at main path
    filters: list of integers, the filters of 3 conv layer at main path
    stage: integer, current stage label, used for generating layer names
    block: 'a','b'..., current block label, used for generating layer names
    use_l2_regularizer: whether to use L2 regularizer on Conv layer.
    batch_norm_decay: Moment of batch norm layers.
    batch_norm_epsilon: Epsilon of batch borm layers.

  Returns:
    Output tensor for the block.
  """
  filters1, filters2, filters3 = filters
  if backend.image_data_format() == 'channels_last':
    bn_axis = 3
  else:
    bn_axis = 1
  conv_name_base = 'res' + str(stage) + block + '_branch'
  bn_name_base = 'bn' + str(stage) + block + '_branch'

  x = layers.Conv2D(
      filters1, (1, 1),
      use_bias=False,
      kernel_initializer='he_normal',
      kernel_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      name=conv_name_base + '2a')(
          input_tensor)
  x = layers.BatchNormalization(
      axis=bn_axis,
      momentum=batch_norm_decay,
      epsilon=batch_norm_epsilon,
      name=bn_name_base + '2a')(
          x)
  x = layers.Activation('relu')(x)

  x = layers.Conv2D(
      filters2,
      kernel_size,
      padding='same',
      use_bias=False,
      kernel_initializer='he_normal',
      kernel_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      name=conv_name_base + '2b')(
          x)
  x = layers.BatchNormalization(
      axis=bn_axis,
      momentum=batch_norm_decay,
      epsilon=batch_norm_epsilon,
      name=bn_name_base + '2b')(
          x)
  x = layers.Activation('relu')(x)

  x = layers.Conv2D(
      filters3, (1, 1),
      use_bias=False,
      kernel_initializer='he_normal',
      kernel_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      name=conv_name_base + '2c')(
          x)
  x = layers.BatchNormalization(
      axis=bn_axis,
      momentum=batch_norm_decay,
      epsilon=batch_norm_epsilon,
      name=bn_name_base + '2c')(
          x)

  x = layers.add([x, input_tensor])
  x = layers.Activation('relu')(x)
  return x


def conv_block(input_tensor,
               kernel_size,
               filters,
               stage,
               block,
               strides=(2, 2),
               use_l2_regularizer=True,
               batch_norm_decay=0.9,
               batch_norm_epsilon=1e-5):
  """A block that has a conv layer at shortcut.

  Note that from stage 3,
  the second conv layer at main path is with strides=(2, 2)
  And the shortcut should have strides=(2, 2) as well

  Args:
    input_tensor: input tensor
    kernel_size: default 3, the kernel size of middle conv layer at main path
    filters: list of integers, the filters of 3 conv layer at main path
    stage: integer, current stage label, used for generating layer names
    block: 'a','b'..., current block label, used for generating layer names
    strides: Strides for the second conv layer in the block.
    use_l2_regularizer: whether to use L2 regularizer on Conv layer.
    batch_norm_decay: Moment of batch norm layers.
    batch_norm_epsilon: Epsilon of batch borm layers.

  Returns:
    Output tensor for the block.
  """
  filters1, filters2, filters3 = filters
  if backend.image_data_format() == 'channels_last':
    bn_axis = 3
  else:
    bn_axis = 1
  conv_name_base = 'res' + str(stage) + block + '_branch'
  bn_name_base = 'bn' + str(stage) + block + '_branch'

  x = layers.Conv2D(
      filters1, (1, 1),
      use_bias=False,
      kernel_initializer='he_normal',
      kernel_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      name=conv_name_base + '2a')(
          input_tensor)
  x = layers.BatchNormalization(
      axis=bn_axis,
      momentum=batch_norm_decay,
      epsilon=batch_norm_epsilon,
      name=bn_name_base + '2a')(
          x)
  x = layers.Activation('relu')(x)

  x = layers.Conv2D(
      filters2,
      kernel_size,
      strides=strides,
      padding='same',
      use_bias=False,
      kernel_initializer='he_normal',
      kernel_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      name=conv_name_base + '2b')(
          x)
  x = layers.BatchNormalization(
      axis=bn_axis,
      momentum=batch_norm_decay,
      epsilon=batch_norm_epsilon,
      name=bn_name_base + '2b')(
          x)
  x = layers.Activation('relu')(x)

  x = layers.Conv2D(
      filters3, (1, 1),
      use_bias=False,
      kernel_initializer='he_normal',
      kernel_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      name=conv_name_base + '2c')(
          x)
  x = layers.BatchNormalization(
      axis=bn_axis,
      momentum=batch_norm_decay,
      epsilon=batch_norm_epsilon,
      name=bn_name_base + '2c')(
          x)

  shortcut = layers.Conv2D(
      filters3, (1, 1),
      strides=strides,
      use_bias=False,
      kernel_initializer='he_normal',
      kernel_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      name=conv_name_base + '1')(
          input_tensor)
  shortcut = layers.BatchNormalization(
      axis=bn_axis,
      momentum=batch_norm_decay,
      epsilon=batch_norm_epsilon,
      name=bn_name_base + '1')(
          shortcut)

  x = layers.add([x, shortcut])
  x = layers.Activation('relu')(x)
  return x


def resnet50(num_classes,
             batch_size=None,
             use_l2_regularizer=True,
             rescale_inputs=False,
             batch_norm_decay=0.9,
             batch_norm_epsilon=1e-5):
  """Instantiates the ResNet50 architecture.

  Args:
    num_classes: `int` number of classes for image classification.
    batch_size: Size of the batches for each step.
    use_l2_regularizer: whether to use L2 regularizer on Conv/Dense layer.
    rescale_inputs: whether to rescale inputs from 0 to 1.
    batch_norm_decay: Moment of batch norm layers.
    batch_norm_epsilon: Epsilon of batch borm layers.

  Returns:
      A Keras model instance.
  """
  input_shape = (224, 224, 3)
  img_input = layers.Input(shape=input_shape, batch_size=batch_size)
  if rescale_inputs:
    # Hub image modules expect inputs in the range [0, 1]. This rescales these
    # inputs to the range expected by the trained model.
    x = layers.Lambda(
        lambda x: x * 255.0 - backend.constant(
            imagenet_preprocessing.CHANNEL_MEANS,
            shape=[1, 1, 3],
            dtype=x.dtype),
        name='rescale')(
            img_input)
  else:
    x = img_input

  if backend.image_data_format() == 'channels_first':
    x = layers.Permute((3, 1, 2))(x)
    bn_axis = 1
  else:  # channels_last
    bn_axis = 3

  block_config = dict(
      use_l2_regularizer=use_l2_regularizer,
      batch_norm_decay=batch_norm_decay,
      batch_norm_epsilon=batch_norm_epsilon)
  x = layers.ZeroPadding2D(padding=(3, 3), name='conv1_pad')(x)
  x = layers.Conv2D(
      64, (7, 7),
      strides=(2, 2),
      padding='valid',
      use_bias=False,
      kernel_initializer='he_normal',
      kernel_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      name='conv1')(
          x)
  x = layers.BatchNormalization(
      axis=bn_axis,
      momentum=batch_norm_decay,
      epsilon=batch_norm_epsilon,
      name='bn_conv1')(
          x)
  x = layers.Activation('relu')(x)
  x = layers.MaxPooling2D((3, 3), strides=(2, 2), padding='same')(x)

  x = conv_block(
      x, 3, [64, 64, 256], stage=2, block='a', strides=(1, 1), **block_config)
  x = identity_block(x, 3, [64, 64, 256], stage=2, block='b', **block_config)
  x = identity_block(x, 3, [64, 64, 256], stage=2, block='c', **block_config)

  x = conv_block(x, 3, [128, 128, 512], stage=3, block='a', **block_config)
  x = identity_block(x, 3, [128, 128, 512], stage=3, block='b', **block_config)
  x = identity_block(x, 3, [128, 128, 512], stage=3, block='c', **block_config)
  x = identity_block(x, 3, [128, 128, 512], stage=3, block='d', **block_config)

  x = conv_block(x, 3, [256, 256, 1024], stage=4, block='a', **block_config)
  x = identity_block(x, 3, [256, 256, 1024], stage=4, block='b', **block_config)
  x = identity_block(x, 3, [256, 256, 1024], stage=4, block='c', **block_config)
  x = identity_block(x, 3, [256, 256, 1024], stage=4, block='d', **block_config)
  x = identity_block(x, 3, [256, 256, 1024], stage=4, block='e', **block_config)
  x = identity_block(x, 3, [256, 256, 1024], stage=4, block='f', **block_config)

  x = conv_block(x, 3, [512, 512, 2048], stage=5, block='a', **block_config)
  x = identity_block(x, 3, [512, 512, 2048], stage=5, block='b', **block_config)
  x = identity_block(x, 3, [512, 512, 2048], stage=5, block='c', **block_config)

  x = layers.GlobalAveragePooling2D()(x)
  x = layers.Dense(
      num_classes,
      kernel_initializer=initializers.RandomNormal(stddev=0.01),
      kernel_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      bias_regularizer=_gen_l2_regularizer(use_l2_regularizer),
      name='fc1000')(
          x)

  # A softmax that is followed by the model loss must be done cannot be done
  # in float16 due to numeric issues. So we pass dtype=float32.
  x = layers.Activation('softmax', dtype='float32')(x)

  # Create model.
  return models.Model(img_input, x, name='resnet50')


def build_model():
  """Constructs the ML model used to predict handwritten digits."""

  model = tf.keras.Sequential([
    tf.keras.layers.Conv2D(16,3,padding="same",activation="relu",input_shape=(224,224,3)),
    tf.keras.layers.MaxPooling2D(),  # 默认池化核为2*2
    tf.keras.layers.Conv2D(32,3,padding="same",activation="relu"),
    tf.keras.layers.MaxPooling2D(),
    tf.keras.layers.Conv2D(64,3,padding="same",activation="relu"),
    tf.keras.layers.MaxPooling2D(),
    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(512,activation="relu"),
    tf.keras.layers.Dense(2,activation="softmax")
  ])
 
  return model


batch_size = 1
img_height = 224
img_width = 224

# model = resnet50(2)
model = build_model()

train_ds = tf.keras.preprocessing.image_dataset_from_directory(
  "cats_and_dogs_filtered/train",
  validation_split=0.2,
  subset="training",
  seed=123,
  label_mode="categorical",
  image_size=(img_height, img_width),
  batch_size=batch_size)

val_ds = tf.keras.preprocessing.image_dataset_from_directory(
  "cats_and_dogs_filtered/validation",
  validation_split=0.2,
  subset="validation",
  seed=123,
  label_mode="categorical",
  image_size=(img_height, img_width),
  batch_size=batch_size)

normalization_layer = layers.experimental.preprocessing.Rescaling(1./255)
normalized_ds = train_ds.map(lambda x, y: (normalization_layer(x), y))
val_normalized_ds = val_ds.map(lambda x, y: (normalization_layer(x), y))

# loss_fn = tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True)
loss_fn = tf.keras.losses.CategoricalCrossentropy(from_logits=False)
# loss_fn = tf.keras.losses.BinaryCrossentropy(from_logits=True)
model.compile(optimizer='adam',
              loss=loss_fn,
              metrics=['accuracy'])
model.fit(
  normalized_ds,
  epochs=25
)
model.evaluate(val_normalized_ds, verbose=2)
model.save('my_model') 
