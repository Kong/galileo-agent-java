package com.mashape.analytics.agent.connection.pool;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class SendAnalyticsTaskTest {
	
	private AtomicInteger val = new AtomicInteger(0);
	
	@Mocked({"get"})
	private MessangerPool messangerPool;

	
	
	private Work mockMessanger = new Messenger(){
		@Override
		public void terminate() {
			val.addAndGet(1);
		}

		@Override
		public void execute(Map<String, Object> analyticsData) {
			val.addAndGet(1);
		}
	};
	
	@Before
	public void setUp() throws Exception {
		new NonStrictExpectations(messangerPool) {
			{
				MessangerPool.get();
				result = mockMessanger;
			}
		};
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test() {
		new SendAnalyticsTask(null).run();
		new SendAnalyticsTask(null).run();
		new SendAnalyticsTask( null).run();
		assertEquals(3, val.get());
	}

}
