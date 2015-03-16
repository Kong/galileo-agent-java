package com.mashape.analytics.agent.connection.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mashape.analytics.agent.connection.pool.ObjectPool;
import com.mashape.analytics.agent.connection.pool.Work;

public class ObjectPoolTest {
	
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
						val.addAndGet(-1);
					}

					@Override
					public void execute(Map<String, Object> analyticsData) {
						val.addAndGet(1);
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
	public void testPoolSize() {
		Work w1 = pool.borrowObject();
		w1.execute(null);
		assertEquals(1, val.get());
		pool.returnObject(w1);
		w1.terminate();
		assertEquals(0, val.get());
		Work w2 = pool.borrowObject();
		w2.execute(null);
		assertEquals(1, val.get());
		Work w3 = pool.borrowObject();
		w3.execute(null);
		assertEquals(2, val.get());
		assertEquals(w1, w3);
		Work w4 = pool.borrowObject();
		w4.execute(null);
		assertEquals(3, val.get());
		assertNotSame(w2, w4);
		pool.returnObject(w2);
		w2.terminate();
		pool.returnObject(w3);
		w3.terminate();
		pool.returnObject(w4);
		w4.terminate();
		assertEquals(0, val.get());
	}

}
