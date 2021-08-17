package org.interpss.service.hz;

import org.interpss.service.AclfTrainDataGenerator;

import com.hazelcast.collection.ItemEvent;
import com.hazelcast.collection.ItemListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class TaskEventListener implements ItemListener<Object> {
	public static final String HzQ_Task = "generate-queue";
	public static final String HzQ_TaskComplete = "finish-queue";
	
	public static final String HzMap_Task = "generate-map";
	public static final String HzMap_Data = "data-map";
	
	public static final String HzKey_Path = "Path";
	public static final String HzKey_Builder = "Builder";
	public static final String HzKey_TrainPoints = "Train_Points";
	public static final String HzKey_InputData = "input";
	public static final String HzKey_OutputData = "output";
	
	// Hz instance
	private HazelcastInstance hz;
	
	/**
	 * Constructor
	 * 
	 * @param client
	 */
	public TaskEventListener(HazelcastInstance client) {
		super();
		this.hz = client;
	}
	
	@Override
	public void itemAdded(ItemEvent<Object> event) {
		System.out.println("Item added: " + event);
		IMap<String, Object> taskMap = hz.getMap(HzMap_Task);
		
		String filename = taskMap.get(HzKey_Path).toString();
		String builderName = taskMap.get(HzKey_Builder).toString();
		int trainPoint= (int) taskMap.get(HzKey_TrainPoints);
		
		AclfTrainDataGenerator dataGen = new AclfTrainDataGenerator();
		dataGen.loadCase(filename, builderName);
		
		String[][] tranSet = dataGen.getTrainSet(trainPoint);
		
		IMap<String,Object> dataMap = hz.getMap(HzMap_Data);
		dataMap.put(HzKey_InputData, tranSet[0]);
		dataMap.put(HzKey_OutputData, tranSet[1]);
		
		hz.getQueue(HzQ_TaskComplete).add("finish");
	}

	@Override
	public void itemRemoved(ItemEvent<Object> event) {
		System.out.println("Item removed: " + event);
	}
}
