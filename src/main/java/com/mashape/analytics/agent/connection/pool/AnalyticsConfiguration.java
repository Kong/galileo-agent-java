package com.mashape.analytics.agent.connection.pool;

import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_ENABLED;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_URL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_TOKEN;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_SIZE_MIN;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;

import com.mashape.analytics.agent.common.Util;
import com.mashape.analytics.agent.filter.AnalyticsFilter;

public class AnalyticsConfiguration {
   
	private String analyticsServerUrl;
	private String environment;
	private String analyticsServerPort;
	private String analyticsToken;
	private boolean isAnlayticsEnabled = false;
	private ThreadPoolExecutor workers;
	
	public static final int DEFAULT_TASK_QUEUE_SIZE = 5000;
	public static final int DEFAULT_WORKER_COUNT_MIN = 0;
	public static final int DEFAULT_WORKER_COUNT_MAX = Runtime.getRuntime().availableProcessors() * 2;
	public static final int DEFAULT_WORKER_KEEPALIVE_TIME = 2;
	
	private static AnalyticsConfiguration config;
	

	final static Logger logger = Logger.getLogger(AnalyticsFilter.class);

	public static class Builder {
		private String analyticsServerUrl;
		private String environment;
		private String analyticsServerPort;
		private String analyticsToken;
		private boolean isAnlayticsEnabled;
		private int workerCountMin;
		private int workerCountMax;
		private int taskQueueSize;
		private BlockingQueue<Runnable> blockingQueue;
		private ThreadPoolExecutor workers;
		private int workerKeepAliveTime;

		public Builder analyticsServerUrl(String analyticsServerUrl) {
			this.analyticsServerUrl = analyticsServerUrl;
			return this;
		}

		public Builder environment(String environment) {
			if(Util.notBlank(environment)){
				this.environment = environment;
			}else{
				this.analyticsServerUrl = "";
			}
			this.environment = environment;
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
			if (!(Util.notBlank(this.analyticsServerUrl )
					&& Util.notBlank(this.analyticsServerPort) && Util.notBlank(this.analyticsToken))) {
				isAnlayticsEnabled = false;
				logger.error("Analytics URl or Port or Token not set");
			}else{
				blockingQueue = new LinkedBlockingQueue<Runnable>(taskQueueSize);
				this.workers = new ThreadPoolExecutor(workerCountMin, workerCountMax, workerKeepAliveTime, TimeUnit.SECONDS, blockingQueue);
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
			MessangerPool.terminate();
			workers.shutdownNow();
			while (!workers.awaitTermination(30, TimeUnit.SECONDS)) {
				logger.debug("Waiting to theads to finish...");
			}
		} catch (InterruptedException e) {
			logger.error("Error during shutdown of analytics pool", e);
		}
	}
	
	public static AnalyticsConfiguration getConfig() {
		if(config == null){
			throw new RuntimeException("Analytics configuration missing");
		}
		return config;
	}
}
