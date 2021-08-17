package org.interpss.service.hz;

import com.hazelcast.collection.ItemEvent;
import com.hazelcast.collection.ItemListener;
import com.hazelcast.core.HazelcastInstance;

public class TaskRequestQListener implements ItemListener<Object> {
	public static final String HzQ_TaskRequest = "TaskRequestQueue";
	public static final String HzQ_TaskReply = "TaskReplyQueue";
	
	// Hz instance
	protected HazelcastInstance hz;
	
	/**
	 * Constructor
	 * 
	 * @param client
	 */
	public TaskRequestQListener(HazelcastInstance client) {
		super();
		this.hz = client;
	}
	
	@Override
	public void itemAdded(ItemEvent<Object> event) {
		processRequest((String)event.getItem());

		// sent a reply messae
		hz.getQueue(HzQ_TaskReply).add("reply msg");
		
		// remove the request message from the request queue
		hz.getQueue(HzQ_TaskRequest).poll();
	}

	@Override
	public void itemRemoved(ItemEvent<Object> event) {
		System.out.println("Item removed: " + event);
	}
	
	protected void processRequest(String msg) {
		System.out.println("Item added: " + msg);
	}
}
