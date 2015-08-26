package com.mashape.analytics.agent.connection.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.mashape.analytics.agent.common.Util;
import com.mashape.analytics.agent.filter.AnalyticsFilter;

public class AnalyticsConfiguration {

	@Override
	public String toString() {
		return "AnalyticsConfiguration [analyticsServerUrl=" + analyticsServerUrl + ", environment=" + environment + ", analyticsServerPort="
				+ analyticsServerPort + ", isAnlayticsEnabled=" + isAnlayticsEnabled + "]";
	}

	private String analyticsServerUrl;
	private String environment;
	private String analyticsServerPort;
	private String analyticsToken;
	private boolean isAnlayticsEnabled = false;
	private ThreadPoolExecutor workers;

	public static final int DEFAULT_TASK_QUEUE_SIZE = 5000;
	public static final int DEFAULT_WORKER_COUNT_MIN = 0;
	public static final int DEFAULT_WORKER_COUNT_MAX = Runtime.getRuntime().availableProcessors() * 2;
	public static final int DEFAULT_WORKER_KEEPALIVE_TIME = 5;

	private static AnalyticsConfiguration config;

	final static Logger LOGGER = Logger.getLogger(AnalyticsFilter.class);

	public static class Builder {
		private String analyticsServerUrl;
		private String environment;
		private String analyticsServerPort;
		private String analyticsToken;
		private boolean isAnlayticsEnabled;
		private int workerCountMin;
		private int workerCountMax;
		private int taskQueueSize;
		private ArrayBlockingQueue<Runnable> blockingQueue;
		private ThreadPoolExecutor workers;
		private int workerKeepAliveTime;

		public Builder analyticsServerUrl(String analyticsServerUrl) {
			this.analyticsServerUrl = analyticsServerUrl;
			return this;
		}

		public Builder environment(String environment) {
			if (Util.notBlank(environment)) {
				this.environment = environment;
			} else {
				this.environment = "";
			}
			return this;
		}

		public Builder analyticsServerPort(String analyticsServerPort) {
			this.analyticsServerPort = analyticsServerPort;
			return this;
		}

		public Builder analyticsToken(String analyticsToken) {
			this.analyticsToken = analyticsToken;
			return this;
		}

		public Builder isAnlayticsEnabled(String isAnlayticsEnabled) {
			this.isAnlayticsEnabled = Boolean.parseBoolean(isAnlayticsEnabled);
			return this;
		}

		public Builder workerCountMin(String workerCountMin) {
			this.workerCountMin = Util.getEnvVarOrDefault(workerCountMin, DEFAULT_WORKER_COUNT_MIN);
			return this;
		}

		public Builder workerCountMax(String workerCountMax) {
			this.workerCountMax = Util.getEnvVarOrDefault(workerCountMax, DEFAULT_WORKER_COUNT_MAX);
			return this;
		}

		public Builder taskQueueSize(String taskQueueSize) {
			this.taskQueueSize = Util.getEnvVarOrDefault(taskQueueSize, DEFAULT_TASK_QUEUE_SIZE);
			return this;
		}

		public Builder workerKeepAliveTime(String workerKeepAliveTime) {
			this.workerKeepAliveTime = Util.getEnvVarOrDefault(workerKeepAliveTime, DEFAULT_WORKER_KEEPALIVE_TIME);
			return this;
		}

		public AnalyticsConfiguration build() {
			if (!this.isAnlayticsEnabled) {
				LOGGER.info("Analytics disabled");
			} else if (!(Util.notBlank(this.analyticsServerUrl) && Util.notBlank(this.analyticsServerPort) && Util.notBlank(this.analyticsToken))) {
				isAnlayticsEnabled = false;
				LOGGER.error("Analytics URl or Port or Token not set");
			} else {
				blockingQueue = new ArrayBlockingQueue<Runnable>(this.taskQueueSize);
				this.workers = new ThreadPoolExecutor(this.workerCountMin, this.workerCountMax, this.workerKeepAliveTime, TimeUnit.SECONDS, this.blockingQueue);
				this.workers.setRejectedExecutionHandler(new RejectedExecutionHandler() {
					@Override
					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						LOGGER.info("Task queue full");
						try {
							LOGGER.info("Sleeping for a second");
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							LOGGER.debug("Failed to sleep", e);
						}
						LOGGER.info("Trying one more time");
						executor.execute(r);
					}
				});
				this.workers.prestartAllCoreThreads();
				LOGGER.info("**********Analytics Configuration************");
				LOGGER.info("Workers count min: " + workerCountMin);
				LOGGER.info("Workers count max: " + workerCountMax);
				LOGGER.info("Workers keep alive time: " + workerKeepAliveTime + " seconds");
				LOGGER.info("Task Queue size: " + taskQueueSize);
				LOGGER.info("Analytics server url: " + analyticsServerUrl);
				LOGGER.info("Analytics server port: " + analyticsServerPort);
				LOGGER.info("Environment: " + environment);
			}

			return (config = new AnalyticsConfiguration(this));
		}
	}

	private AnalyticsConfiguration(Builder builder) {
		this.analyticsServerUrl = builder.analyticsServerUrl;
		this.environment = builder.environment;
		this.analyticsServerPort = builder.analyticsServerPort;
		this.analyticsToken = builder.analyticsToken;
		this.isAnlayticsEnabled = builder.isAnlayticsEnabled;
		this.workers = builder.workers;
	}

	public String getAnalyticsServerUrl() {
		return analyticsServerUrl;
	}

	public String getEnvironment() {
		return environment;
	}

	public String getAnalyticsServerPort() {
		return analyticsServerPort;
	}

	public String getAnalyticsToken() {
		return analyticsToken;
	}

	public boolean isAnlayticsEnabled() {
		return isAnlayticsEnabled;
	}

	public ThreadPoolExecutor getWorkers() {
		return workers;
	}

	public void shutdown() {
		try {
			MessengerPool.terminate();
			workers.shutdown();
			while (!workers.awaitTermination(30, TimeUnit.SECONDS)) {
				LOGGER.debug("Waiting to theads to finish...");
			}
		} catch (InterruptedException e) {
			LOGGER.error("Error during shutdown of analytics pool", e);
		}
	}

	public static AnalyticsConfiguration getConfig() {
		if (config == null) {
			throw new RuntimeException("Analytics configuration missing");
		}
		return config;
	}

	public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
		this.workers.setRejectedExecutionHandler(handler);
	}
}
