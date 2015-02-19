/*
The MIT License
Copyright (c) 2013 Mashape (http://mashape.com)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
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
