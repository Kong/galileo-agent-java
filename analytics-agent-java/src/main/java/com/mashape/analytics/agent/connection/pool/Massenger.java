package com.mashape.analytics.agent.connection.pool;

import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_DATA;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_URL;

import java.util.Map;

import org.zeromq.ZMQ;

public class Massenger implements Work {
	
	private ZMQ.Context context = ZMQ.context(1);
	private ZMQ.Socket socket = context.socket(ZMQ.PUSH);
	
	public void execute(Map<String, String> analyticsData){
			String msg = analyticsData.get(ANALYTICS_DATA);
			String analyticsServerUrl = analyticsData.get(ANALYTICS_SERVER_URL);
			String port = analyticsData.get(ANALYTICS_SERVER_PORT);

			ZMQ.Context context = ZMQ.context(1);
			ZMQ.Socket socket = context.socket(ZMQ.PUSH);
			socket.connect("tcp://" + analyticsServerUrl + ":" + port);
			socket.send(msg);	
	}
	
	public void terminate(){
		if(socket != null){
			socket.close();
		}
		if(context != null){
			context.term();
		}
	}
}
