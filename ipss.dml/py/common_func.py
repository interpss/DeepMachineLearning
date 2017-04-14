from py4j.java_gateway import JavaGateway

#
# define InterPSS train/test case service 
#
gateway = JavaGateway()
ipss_app = gateway.entry_point

#
# define configuration parameters
#
learning_rate = 0.001
train_steps = 10000

# function to transfer data from a tensor array to a Java double array
def transfer2JavaDblAry(tArray, size):
    dblAry = gateway.new_array(gateway.jvm.double, size)
    i = 0
    for x in tArray:
        dblAry[i] = float(x)
        i = i + 1
    return dblAry

def printArray(ary, msg) :
    print(msg)
    for x in ary :
        print(x)
        
def print2DArray(ary2D, msg1, msg2) :
    print(msg1)
    for ary in ary2D :
        print(msg2)
        for x in ary :
            print(x)        