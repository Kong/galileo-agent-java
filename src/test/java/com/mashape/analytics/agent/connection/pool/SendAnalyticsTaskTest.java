package com.mashape.analytics.agent.connection.pool;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeromq.ZContext;

@RunWith(JMockit.class)
public class SendAnalyticsTaskTest {

	private AtomicInteger val = new AtomicInteger(0);

	@Mocked({ "get" })
	private MessengerPool messangerPool;

	@Mocked
	private Messenger messenger;

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		new MockUp<Messenger>() {
			@Mock
			public void $init(ZContext context) {
			}
			
			@Mock
			public void execute(Map<String, Object> analyticsData) {
				val.addAndGet(1);
			}
		};
		new NonStrictExpectations(messangerPool) {
			{
				MessengerPool.get();
				result = new Messenger((ZContext) any);
			}
		};

		new SendAnalyticsTask(null).run();
		new SendAnalyticsTask(null).run();
		new SendAnalyticsTask(null).run();
		assertEquals(3, val.get());
	}

}
