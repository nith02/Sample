import sys
import tensorflow as tf
import cv2
import numpy as np
from pprint import pprint

path_1 = "./my_model.tflite"
# path_1 = "./mobilenet_v1_1.0_224_quant.tflite"
interpreter = tf.lite.Interpreter(path_1)
interpreter.allocate_tensors()
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()
print(input_details, '\n', output_details)

path = sys.argv[1]
image = cv2.imread(path)
image = image.astype(np.float32)
image = cv2.resize(image, (224, 224))
# image = image.astype(np.uint8)
data = np.expand_dims(image, axis=0)
data = data / 255.0
interpreter.set_tensor(0, data)
interpreter.invoke()
out = interpreter.get_tensor(21)
pprint(out)
