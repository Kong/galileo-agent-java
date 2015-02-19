package com.mashape.analytics.agent.connection.pool;

import java.util.Map;

/*
 * @author shashi
 * 
 * Task use a pooled object to send data
 */
public class SendAnalyticsTask implements Runnable {

	private ObjectPool<Work> pool;
	private Map<String, Object> analyticsData;

	public SendAnalyticsTask(ObjectPool<Work> pool, Map<String, Object> analyticsData) {
		this.pool = pool;
		this.analyticsData = analyticsData;
	}

	public void run() {
		Work work = pool.borrowObject();
		work.execute(analyticsData);
		pool.returnObject(work);
	}
}
