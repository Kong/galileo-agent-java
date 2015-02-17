package com.mashape.analytics.agent.connection;

import java.util.Map;

import org.zeromq.ZMQ;

public class ConnectionManager {

	public static void sendMessage(Map<String, String> analyticsData) {
		String msg = analyticsData.get("data");
		String analyticsServerUrl = analyticsData.get("analyticsServerUrl");
		String port = analyticsData.get("analyticsServerPort");

		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.PUSH);
		socket.connect("tcp://" + analyticsServerUrl + ":" + port);
		socket.send(msg);
		socket.close();
		context.term();
	}
}
