from datetime import datetime

import tensorflow as tf
import numpy as np

from common_func import ipss_app
from common_func import learning_rate
from common_func import train_steps
from common_func import transfer2JavaDblAry
from common_func import printArray

train_points = 50

# 
# load the IEEE-14Bus case
#
filename = 'c:/temp/temp/ieee14.ieee'
noBus, noBranch = ipss_app.loadCase(filename, 'BusVoltLoadChangeTrainCaseBuilder')
print(filename, ' loaded,  no of Buses, Branches:', noBus, ', ', noBranch)

# define model size
size = noBus * 2
#print('size: ', size)

# define model variables
W1 = tf.Variable(tf.zeros([size,noBranch]))
b1 = tf.Variable(tf.zeros([noBranch]))

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

# run the computation graph
with tf.Session() as sess :
    sess.run(init)
    
    # run the training part
    # =====================
    
    print('Begin training: ', datetime.now())
    
    # retrieve training set
    trainSet = ipss_app.getTrainSet(train_points);
    train_x = np.array([trainSet[0]])[0]
    train_y = np.array([trainSet[1]])[0]
    
    # run the training part
    for i in range(train_steps):
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
    for factor in [0.45, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.55] :
    #for factor in [0.45, 1.0, 1.55] :
        testCase = ipss_app.getTestCase(factor)
        test_x = np.array([testCase[0]])[0]
        test_y = np.array([testCase[1]])[0]        
           
        # compute model output (network voltage)
        model_y = sess.run(nn_model(x), {x:[test_x]})
        #printArray(model_y, 'model_y')
       
        netVoltage = transfer2JavaDblAry(model_y[0], size)
        print('model out mismatch: ', ipss_app.getMismatchInfo(netVoltage))
