import hazelcast
import sys

# 
#  constants
#

TaskRquestQNamd = "TaskRequestQueue"
TaskReplyQName  = "TaskReplyQueue"

# start the Hz client
client = hazelcast.HazelcastClient()

# get the request and reply queue
qRequest = client.get_queue(TaskRquestQNamd).blocking()
qReply = client.get_queue(TaskReplyQName).blocking()

# define request msg
reqMsg = "request msg"

# send a request message
qRequest.put(reqMsg)

# wait for reply msg
replyMsg = qReply.take();

print("reply: " + replyMsg)

# shut down the Hz client
client.shutdown()
