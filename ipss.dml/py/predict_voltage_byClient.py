import hazelcast
import lib.common_func as cf
import tensorflow as tf
import sys
import numpy as np

from datetime import datetime
config = hazelcast.ClientConfig()
client = hazelcast.HazelcastClient(config)

generate_map = client.get_map("generate-map").blocking()
generate_map.put("Path", "testdata/cases/ieee14.ieee")
generate_map.put("Builder", "BusVoltLoadChangeTrainCaseBuilder")
generate_map.put("Train_Points", 50)

queue = client.get_queue("generate-queue").blocking()
queue_f = client.get_queue("finish-queue").blocking()
data_map = client.get_map("data-map").blocking()
queue.offer("generate")

queue_f.take();

input = data_map.get("input")
output = data_map.get("output")
trainSet = [input,output]
#print(trainSet)
train_x, train_y = cf.transfer2PyArrays(trainSet)
size = len(input[0].split())
#print('size: ', size)

# define model variables
W = tf.Variable(tf.zeros([size,size]))
b = tf.Variable(tf.zeros([size]))

init = tf.global_variables_initializer();

# define model

def nn_model(data):
    output = tf.matmul(data, W) + b
    return output

# define loss 
x = tf.placeholder(tf.float32, [None, size])
y = tf.placeholder(tf.float32)

error = tf.square(nn_model(x) - y)
loss = tf.reduce_sum(error)

# define training optimization
optimizer = tf.train.GradientDescentOptimizer(cf.learning_rate)
train = optimizer.minimize(loss)

# run the computation graph
with tf.Session() as sess :
    sess.run(init)
    
    # run the training part
    # =====================
 
    print('Begin training: ', datetime.now())
    
    # run the training part
    for i in range(cf.train_steps):
        if (i % 1000 == 0) : print('Training step: ', i) 
        sess.run(train, {x:train_x, y:train_y})

    print('End training: ', datetime.now())
    generate_map.put("Train_Points", 1)
    
    queue.offer("generate")
    queue_f.take();
    input = data_map.get("input")
    output = data_map.get("output")
    trainSet = [input,output]
    test_x, test_y = cf.transfer2PyArrays(trainSet)
    model_y = sess.run(nn_model(x), {x:test_x})
    print('Max error: ', np.sqrt(np.max(np.abs(np.square(model_y - test_y)))))
client.shutdown()
