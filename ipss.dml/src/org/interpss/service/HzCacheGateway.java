package org.interpss.service;

import java.io.IOException;

import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.service.hz.TaskEventListener;
import org.interpss.service.hz.TaskRequestQListener;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.interpss.common.exp.InterpssException;
import com.interpss.common.exp.IpssCacheException;

public class HzCacheGateway {
	public static void main(String[] args)
			throws IOException, InterpssException, IpssNumericException, IpssCacheException {
		// config Hz server
		Config config = new Config();
		NetworkConfig network = config.getNetworkConfig();
		network.setPublicAddress("127.0.0.1");
		
		// start a Hz instance
		HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
		
		// add debug info
		hz.addDistributedObjectListener(new DistributedObjectListener() {
			@Override
			public void distributedObjectCreated(DistributedObjectEvent event) {
				DistributedObject instance = event.getDistributedObject();
				System.out.println("Created " + instance.getName() + ", " + instance.getServiceName());
			}

			@Override
			public void distributedObjectDestroyed(DistributedObjectEvent event) {
				DistributedObject instance = event.getDistributedObject();
				System.out.println("Destroyed " + instance.getName() + ", " + instance.getServiceName());
			}
		});
		
		// add task queue listener
		hz.getQueue(TaskEventListener.HzQ_Task).addItemListener(new TaskEventListener(hz), true);
		
		hz.getQueue(TaskRequestQListener.HzQ_TaskRequest).addItemListener(new TaskRequestQListener(hz), true);
	}
}