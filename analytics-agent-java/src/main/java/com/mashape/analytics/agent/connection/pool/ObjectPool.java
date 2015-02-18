package com.mashape.analytics.agent.connection.pool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ObjectPool<E extends Work> {
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
		E object = null;
		if ((object = pool.poll()) == null) {
			object = createPoolObject();
		}
		return object;
	}

	public void returnObject(E object) {
		if (object == null) {
			return;
		}
		pool.offer(object);
	}

	public void removeObject(int toBeRemoved) {
		for (int i = 0; i < toBeRemoved; i++) {
			E poolObject = pool.poll();
			poolObject.terminate();
		}
	}

	public void createObject(int tobeCreated) {
		if (pool == null) {
			pool = new ConcurrentLinkedQueue<E>();
		}
		for (int i = 0; i < tobeCreated; i++) {
			pool.add(createPoolObject());
		}
	}

	public abstract E createPoolObject();

	public void terminate() {
		if (executor != null) {
			executor.shutdown();
		}
	}
}
