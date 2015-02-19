package com.mashape.analytics.agent.connection.pool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Generic object pool of type Work.
 * 
 * @author Shashi
 *
 * @param <E>
 */
public abstract class ObjectPool<E extends Work> {
	private static final Logger logger = Logger.getLogger(ObjectPool.class);

	private ConcurrentLinkedQueue<E> pool;
	private ScheduledExecutorService executor;

	public ObjectPool(int minThread) {
		createObject(minThread);
	}

	public ObjectPool(final int minPoolSize, final int maxPoolSize, int interval) {
		createObject(minPoolSize);
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(new Runnable() {

			public void run() {
				int size = pool.size();
				logger.debug("Pool size:" + size);
				if (size < minPoolSize) {
					createObject(minPoolSize - size);
				} else if (size > maxPoolSize) {
					removeObject(size - maxPoolSize);
				}
			}
		}, interval, interval, TimeUnit.SECONDS);
	}

	public E borrowObject() {
		E poolObject = null;
		if ((poolObject = pool.poll()) == null) {
			poolObject = createPoolObject();
		}
		logger.debug("Object borrowed from pool:" + poolObject.toString());
		return poolObject;
	}

	public void createObject(int tobeCreated) {
		if (pool == null) {
			pool = new ConcurrentLinkedQueue<E>();
		}
		for (int i = 0; i < tobeCreated; i++) {
			E poolObject = createPoolObject();
			logger.debug("New object added to pool:" + poolObject.toString());
			pool.add(createPoolObject());
		}
	}

	public abstract E createPoolObject();

	public void removeObject(int toBeRemoved) {
		for (int i = 0; i < toBeRemoved; i++) {
			E poolObject = pool.poll();
			poolObject.terminate();
			logger.debug("Object removed from pool:" + poolObject.toString());
		}
	}

	public void returnObject(E poolObject) {
		if (poolObject == null) {
			return;
		}
		pool.offer(poolObject);
		logger.debug("Object returned to pool:" + poolObject.toString());
	}

	public void terminate() {
		if (executor != null) {
			executor.shutdown();
		}
		if (pool != null) {
			removeObject(pool.size());
		}
	}
}
