package com.mashape.analytics.agent.connection.pool;

import java.util.Map;

public interface Work {
	void terminate();
	void execute(Map<String, Object> analyticsData);
}
