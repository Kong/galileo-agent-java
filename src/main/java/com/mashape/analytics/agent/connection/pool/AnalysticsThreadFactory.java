package com.mashape.analytics.agent.connection.pool;

import java.util.concurrent.ThreadFactory;

public class AnalysticsThreadFactory implements ThreadFactory {

	@Override
	public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		return new Thread(r);
	}

}
