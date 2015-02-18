package com.mashape.analytics.agent.connection.pool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public abstract class ObjectPool<E extends Work> {
	Logger logger = Logger.getLogger(ObjectPool.class);

	private ConcurrentLinkedQueue<E> pool;
	private ScheduledExecutorService executor;

	public ObjectPool(int minThread) {
		createPool(minThread);
	}

	public ObjectPool(final int minThread, final int maxThread, int interval) {
		createObject(minThread);

		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(new Runnable() {

			public void run() {
				int size = pool.size();
				if (size < minThread) {
					createObject(minThread - size);
				} else if (size > maxThread) {
					removeObject(size - maxThread);
				}
			}
		}, interval, interval, TimeUnit.SECONDS);
	}

	private void createPool(int minThread) {
		pool = new ConcurrentLinkedQueue<E>();
		for (int i = 0; i < minThread; i++) {
			pool.add(createPoolObject());
		}

	}

	public E borrowObject() {
		E poolObject = null;
		if ((poolObject = pool.poll()) == null) {
			poolObject = createPoolObject();
		}
		logger.debug("Object borrowed from pool:" + poolObject.toString());
		return poolObject;
	}

	public void returnObject(E poolObject) {
		if (poolObject == null) {
			return;
		}
		pool.offer(poolObject);
		logger.debug("Object returned to pool:" + poolObject.toString());
	}

	public void removeObject(int toBeRemoved) {
		for (int i = 0; i < toBeRemoved; i++) {
			E poolObject = pool.poll();
			poolObject.terminate();
			logger.debug("new object removed from pool:"
					+ poolObject.toString());
		}
	}

	public void createObject(int tobeCreated) {
		if (pool == null) {
			pool = new ConcurrentLinkedQueue<E>();
		}
		for (int i = 0; i < tobeCreated; i++) {
			E poolObject = createPoolObject();
			logger.debug("new object added to pool:" + poolObject.toString());
			pool.add(createPoolObject());
		}
	}

	public abstract E createPoolObject();

	public void terminate() {
		if (executor != null) {
			executor.shutdown();
		}
		if (pool != null) {
			removeObject(pool.size());
		}
	}
}
