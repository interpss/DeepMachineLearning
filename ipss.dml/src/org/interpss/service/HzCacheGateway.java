package org.interpss.service;

import java.io.IOException;

import org.interpss.numeric.exp.IpssNumericException;

import com.hazelcast.collection.ItemEvent;
import com.hazelcast.collection.ItemListener;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.interpss.common.exp.InterpssException;
import com.interpss.common.exp.IpssCacheException;

public class HzCacheGateway {
	public static void main(String[] args)
			throws IOException, InterpssException, IpssNumericException, IpssCacheException {
		Config config = new Config();
		NetworkConfig network = config.getNetworkConfig();
		network.setPublicAddress("127.0.0.1");
		HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
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
		
		hz.getQueue("generate-queue").addItemListener(new GenerateEventListener(hz), true);
	}
	
	public static class GenerateEventListener implements ItemListener<Object> {
		private HazelcastInstance hz;
		
		public GenerateEventListener(HazelcastInstance client) {
			super();
			this.hz = client;
		}
		@Override
		public void itemAdded(ItemEvent<Object> event) {
			System.out.println("Item added: " + event);
			IMap<Object, Object> generateMap = hz.getMap("generate-map");
			
			AclfTrainDataGenerator dataGen = new AclfTrainDataGenerator();
			//read case
			String filename = generateMap.get("Path").toString();
			dataGen.loadCase(filename, generateMap.get("Builder").toString());
			int trainPoint= (int) generateMap.get("Train_Points");
			String[][] tranSet = dataGen.getTrainSet(trainPoint);
			hz.getMap("data-map").put("input",tranSet[0]);
			hz.getMap("data-map").put("output", tranSet[1]);
			hz.getQueue("finish-queue").add("finish");
		}

		@Override
		public void itemRemoved(ItemEvent<Object> event) {
			System.out.println("Item removed: " + event);
		}
	}
}