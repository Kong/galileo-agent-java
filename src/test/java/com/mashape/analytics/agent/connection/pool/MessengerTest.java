package com.mashape.analytics.agent.connection.pool;

import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_DATA;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_URL;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.mashape.analytics.agent.mapper.AnalyticsDataMapper;
import com.mashape.analytics.agent.modal.Entry;

@RunWith(JMockit.class)
public class MessengerTest {

	
	@Mocked
	@Injectable
	private Context context;

	@Mocked
	@Injectable
	private Socket socket;

	@Tested
	private Messenger subject;

	@Test
	public void test() {
		Map<String, Object> analyticsData = getMockedData();
		new NonStrictExpectations() {
			{
				context.socket(anyInt);
				result = socket;
				socket.connect(anyString);
				socket.send(anyString);
			}
		};
		
		subject.execute(analyticsData);
	}

	private Map<String, Object> getMockedData() {
		Map<String, Object> analyticsData = new HashMap<String, Object>();
		analyticsData.put(ANALYTICS_SERVER_URL, "abc.com");
		analyticsData.put(ANALYTICS_SERVER_PORT, "5000");
		Entry entry = new Entry();
		analyticsData.put(ANALYTICS_DATA, entry);
		return analyticsData;
	}
	 


}
