import tensorflow as tf
import numpy as np

from py4j.java_gateway import JavaGateway

#
# define InterPSS train/test case service 
#
gateway = JavaGateway()
ipss_app = gateway.entry_point

# 
# load the IEEE-14Bus case
#
filename = 'c:/temp/temp/ieee14.ieee'
noBus = ipss_app.loadCase(filename)
print(filename, ' loaded,  noBus:', noBus)

# define model size
size = noBus * 2
print('size: ', size)

# define model variables
W1 = tf.Variable(tf.zeros([size,size]))
W2 = tf.Variable(tf.zeros([size,size]))
W3 = tf.Variable(tf.zeros([size,size]))

b1 = tf.Variable(tf.zeros([size]))
b2 = tf.Variable(tf.zeros([size]))
b3 = tf.Variable(tf.zeros([size]))

init = tf.initialize_all_variables()

# define model

def nn_model(data):
    layer1 = tf.matmul(data, W1) + b1
    layer1 = tf.nn.relu(layer1)
    
    layer2 = tf.matmul(layer1, W2) + b2
    layer2 = tf.nn.relu(layer2)

    output = tf.matmul(layer2, W3) + b3
    
    return output

# define loss 
x = tf.placeholder(tf.float32, [None, size])
y = tf.placeholder(tf.float32)

error = tf.square(nn_model(x) - y)
loss = tf.reduce_sum(error)

# define training
optimizer = tf.train.GradientDescentOptimizer(0.0001)
train = optimizer.minimize(loss)

# transfer data from a tensor array to a double array
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
    
    # retrieve training set
    trainSet = ipss_app.getTrainSet(100);
    train_x = np.array([trainSet])[0][0]
    train_y = np.array([trainSet])[0][1]
    
    # run the training part
    for i in range(10000):
        if (i % 1000 == 0) : print('Optimization steps: ', i) 
        sess.run(train, { x:train_x, y:train_y})

    # run the verification part
    testSet = ipss_app.getTrainSet(1);
    test_x = np.array([testSet])[0][0]
    #test_y = np.array([testSet])[0][1]
    
    # compute model output (network voltage
    model_y = sess.run(nn_model(x), {x:test_x})
    '''
    print('model_y')
    for x in model_y[0] :
        print(x)
    '''
    netVoltage = transfer2DblAry(model_y[0])
    print('model solution mismatch: ', ipss_app.getMismatchInfo(netVoltage))
