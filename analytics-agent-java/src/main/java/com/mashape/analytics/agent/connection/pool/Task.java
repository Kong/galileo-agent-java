package com.mashape.analytics.agent.connection.pool;

import java.util.Map;


public class Task implements Runnable {
	
	private ObjectPool<Work> pool;
	private Map<String, String> analyticsData;
	
	
	public Task(ObjectPool<Work> pool, Map<String, String> analyticsData){
		this.pool = pool;
		this.analyticsData = analyticsData;
	}

	public void run() {
		Work work = pool.borrowObject();
		work.execute(analyticsData);
		pool.returnObject(work);
	}
}
