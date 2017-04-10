from datetime import datetime

import tensorflow as tf
import numpy as np

from py4j.java_gateway import JavaGateway

#
# define InterPSS train/test case service 
#
gateway = JavaGateway()
ipss_app = gateway.entry_point

learning_rate = 0.001
train_points = 50

# 
# load the IEEE-14Bus case
#
filename = 'c:/temp/temp/ieee14.ieee'
noBus = ipss_app.loadCase(filename)
print(filename, ' loaded,  no of Buses:', noBus)

# define model size
size = noBus * 2
#print('size: ', size)

# define model variables
W1 = tf.Variable(tf.zeros([size,size]))

b1 = tf.Variable(tf.zeros([size]))

init = tf.initialize_all_variables()

# define model

def nn_model(data):
    output = tf.matmul(data, W1) + b1
    return output

# define loss 
x = tf.placeholder(tf.float32, [None, size])
y = tf.placeholder(tf.float32)

error = tf.square(nn_model(x) - y)
loss = tf.reduce_sum(error)

# define training optimization
optimizer = tf.train.GradientDescentOptimizer(learning_rate)
train = optimizer.minimize(loss)

# function to transfer data from a tensor array to a double array
def transfer2DblAry(tArray):
    dblAry = gateway.new_array(gateway.jvm.double, size)
    i = 0
    for x in tArray:
        dblAry[i] = float(x)
        i = i + 1
    return dblAry

# run the computation graph
with tf.Session() as sess :
    sess.run(init)
    
    # run the training part
    # =====================
 
    print('Begin training: ', datetime.now())
     
    # retrieve training set
    trainSet = ipss_app.getTrainSet(train_points);
    train_x = np.array([trainSet])[0][0]
    train_y = np.array([trainSet])[0][1]
    
    # run the training part
    for i in range(10000):
        if (i % 1000 == 0) : print('Training step: ', i) 
        sess.run(train, {x:train_x, y:train_y})

    print('End training: ', datetime.now())
    '''
    print('W1: ', sess.run(W1))
    print('b1: ', sess.run(b1))
    '''
 
    # run the verification part
    # =========================
    
    # retrieve a test case
    testCase = ipss_app.getTestCase();
    test_x = np.array([testCase])[0][0]
    '''
    test_y = np.array([testCase])[0][1]
    print('test_y')
    for a in test_y :
        print(a)
    '''  
    
    # compute model output (network voltage)
    model_y = sess.run(nn_model(x), {x:[test_x]})
    '''
    print('model_y')
    for x in model_y[0] :
        print(x)
    '''
    
    netVoltage = transfer2DblAry(model_y[0])
    print('model out mismatch: ', ipss_app.getMismatchInfo(netVoltage))
