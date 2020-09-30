# http://www.image-net.org/download-images
# https://github.com/tensorflow/models.git
# https://storage.googleapis.com/mledu-datasets/cats_and_dogs_filtered.zip

sudo pip3 install -U virtualenv

virtualenv --system-site-packages -p python3 ./venv
source ./venv/bin/activate

pip3 install --upgrade tensorflow
pip3 install tf-models-official

python3 -m tensorflow_datasets.scripts.download_and_prepare --datasets=imagenet2012

python3 -m official.vision.image_classification.classifier_trainer --mode=train_and_eval \
  --model_type=resnet \
  --dataset=imagenet \
  --model_dir=$MODEL_DIR \
  --data_dir=$DATA_DIR \
  --config_file=configs/examples/resnet/imagenet/gpu.yaml

python3 -m official.vision.image_classification.mnist_main \
  --model_dir=$MODEL_DIR \
  --data_dir=$DATA_DIR \
  --train_epochs=10 \
  --distribution_strategy=one_device \
  --download
