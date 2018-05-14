'''
    Copyright (C) 2005-17 www.interpss.org
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
'''

'''
 Use NN-model to predict the bus voltage for a set of scale-factors
'''

from datetime import datetime
import numpy as np
import tensorflow as tf

import sys
sys.path.insert(0, '../..')

import lib.common_func as cf

train_points = 1000

# 
# load the IEEE-14Bus case
#
filename = 'c:/temp/temp/ieee14.ieee'
noBus, noBranch = cf.ipss_app.loadCase(filename, 'BusVoltLoadChangeRandomTrainCaseBuilder')
print(filename, ' loaded,  no of Buses, Branches:', noBus, ', ', noBranch)

# define model size
size = noBus * 2
#print('size: ', size)

# define model variables
W1 = tf.Variable(tf.zeros([size*2,size]))
b1 = tf.Variable(tf.zeros([size]))



# define model

def nn_model(data):
    output = tf.matmul(data, W1) + b1
    return output

# define loss 
x = tf.placeholder(tf.float32, [None, size*2])
y = tf.placeholder(tf.float32)

error = tf.square(nn_model(x) - y)
loss = tf.reduce_sum(error)

# define training optimization
optimizer = tf.train.AdagradOptimizer(0.3)
train = optimizer.minimize(loss)
init = tf.global_variables_initializer()
# run the computation graph
with tf.Session() as sess :
    sess.run(init)
    
    # run the training part
    # =====================
    
    print('Begin training: ', datetime.now())
    
    # retrieve training set
    trainSet = cf.ipss_app.getTrainSet(train_points)
    train_x, train_y = cf.transfer2PyArrays(trainSet)
    train_x,aver_x,ran_x = cf.normalization(train_x);
    
    train_y,aver_y,ran_y = cf.normalization(train_y);
    # run the training part
    for i in range(cf.train_steps):
        if (i % 1000 == 0) : print('Training step: ', i) 
        sess.run(train, {x:train_x, y:train_y})

    print('End training: ', datetime.now())
    
    '''
    print('W1: ', sess.run(W1))
    print('b1: ', sess.run(b1))
    '''
    
    # run the verification part
    # =========================
    testSize=100
    mismatchSet = np.zeros((testSize,2))
    misSet = np.zeros((testSize,size))
    # retrieve a test case
    for i in range(testSize) :
    #for factor in [0.45, 1.0, 1.55] :
        testCase = cf.ipss_app.getTestCase()
        test_x, test_y = cf.transfer2PyArrays(testCase)        
        test_x =  np.divide(np.subtract(test_x,aver_x),ran_x)
        # compute model output (network voltage)
        model_y = sess.run(nn_model(x), {x:test_x})
        #printArray(model_y, 'model_y')
        misSet[i] =  np.abs(model_y[0]*ran_y+aver_y-test_y[0])
       
#         netVoltage = cf.transfer2JavaDblAry(model_y[0]*ran_y+aver_y, size)
#         mismatchSet[i] = np.array([cf.ipss_app.getMismatch(netVoltage)[0],cf.ipss_app.getMismatch(netVoltage)[1]])
#     train_mm,aver_mm,ran_mm = cf.normalization(mismatchSet);
    train_m,aver_m,ran_m = cf.normalization(misSet);
#     print('model out mismatch(aver): ', aver_mm)
#     print('model out mismatch(range): ', ran_mm)
#     print('aver case max error: ', aver_m)
#     print('max case max error : ', ran_m )
    print('max case max error : ', np.max(ran_m) )
    print('aver case max error : ', np.average(np.max(misSet, axis =1)) )
