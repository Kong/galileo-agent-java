package com.mashape.analytics.agent.connection.pool;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SendAnalyticsTaskTest {

	private ObjectPool<Work> pool;
	private AtomicInteger val = new AtomicInteger(0);

	@Before
	public void setUp() throws Exception {
		pool = new ObjectPool<Work>(2, 4,
				5) {
			@Override
			public Work createPoolObject() {
				return new Work(){

					@Override
					public void terminate() {
						int  sum = val.addAndGet(-1);
					}

					@Override
					public void execute(Map<String, Object> analyticsData) {
						int  sum = val.addAndGet(1);
					}
				};
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		pool.terminate();
	}
	
	
	@Test
	public void test() {
		new SendAnalyticsTask(pool, null).run();
		new SendAnalyticsTask(pool, null).run();
		new SendAnalyticsTask(pool, null).run();
		assertEquals(3, val.get());
		pool.terminate();
		assertEquals(1, val.get());
	}

}
