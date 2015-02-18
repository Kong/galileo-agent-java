package com.mashape.analytics.agent.connection.pool;

import static com.mashape.analytics.agent.common.AnalyticsConstants.AGENT_NAME;
import static com.mashape.analytics.agent.common.AnalyticsConstants.AGENT_VERSION;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_DATA;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_URL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.HAR_VERSION;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SERVICE_TOKEN;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

import com.google.gson.Gson;
import com.mashape.analytics.agent.modal.Creator;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.modal.Har;
import com.mashape.analytics.agent.modal.Log;
import com.mashape.analytics.agent.modal.Message;

public class Messenger implements Work {

	Logger logger = Logger.getLogger(Messenger.class);

	private ZMQ.Context context = ZMQ.context(1);
	private ZMQ.Socket socket = context.socket(ZMQ.PUSH);

	public void execute(Map<String, Object> analyticsData) {
		Entry entry = (Entry) analyticsData.get(ANALYTICS_DATA);
		Message msg = getMessage(entry);
		String data = new Gson().toJson(msg);
		String analyticsServerUrl = analyticsData.get(ANALYTICS_SERVER_URL).toString();
		String port = analyticsData.get(ANALYTICS_SERVER_PORT).toString();
		
		socket.connect("tcp://" + analyticsServerUrl + ":" + port);
		socket.send(data);
		logger.debug("Message sent:" + msg);
	}

	public void terminate() {
		if (socket != null) {
			logger.debug("Closing socket:" + socket.toString());
			socket.close();
		}
		if (context != null) {
			context.term();
		}
	}

	public Message getMessage(Entry entry) {
		Message message = new Message();
		message.setHar(setHar(entry));
		message.setServiceToken(System.getenv(SERVICE_TOKEN));
		return message;
	}

	private Har setHar(Entry entry) {
		Har har = new Har();
		har.setLog(setLog(entry));
		return har;
	}

	private Log setLog(Entry entry) {
		Log log = new Log();
		log.setVersion(HAR_VERSION);
		log.setCreator(setCreator());
		log.getEntries().add(entry);
		return log;
	}

	private Creator setCreator() {
		Creator creator = new Creator();
		creator.setName(AGENT_NAME);
		creator.setVersion(AGENT_VERSION);
		return creator;
	}
}
