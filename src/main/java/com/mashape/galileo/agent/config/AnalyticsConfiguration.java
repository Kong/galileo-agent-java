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

package com.mashape.galileo.agent.config;

import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_ANALYTICS_BATCH_SIZE;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_ANALYTICS_FLUSH_INTERVAL;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_ANALYTICS_HOST;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_ANALYTICS_QUEUE_SIZE;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_ANALYTICS_RETRY_COUNT;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_ANALYTICS_SERVER_PATH;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_ANALYTICS_SERVER_PORT;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_BATCHED_ALF_ALLOWED_SIZE_MB;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_CONNECTION_COUNT;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_CONNECTION_KEEP_ALIVE_TIME;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_SEND_BODY;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_SINGLE_ALF_ALLOWED_SIZE_MB;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_TICKER_INTERVAL_TIME;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_USE_HTTPS;
import static com.mashape.galileo.agent.common.AnalyticsConstants.DEFAULT_WORKER_COUNT_MAX;
import static com.mashape.galileo.agent.common.AnalyticsConstants.MAX_ANALYTICS_BATCH_SIZE;
import static com.mashape.galileo.agent.common.AnalyticsConstants.MAX_ANALYTICS_FLUSH_INTERVAL;
import static com.mashape.galileo.agent.common.AnalyticsConstants.MAX_ANALYTICS_RETRY_COUNT;
import static com.mashape.galileo.agent.common.AnalyticsConstants.MAX_CONNECTION_KEEP_ALIVE_TIME;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.mashape.galileo.agent.common.PropertyUtil;
import com.mashape.galileo.agent.modal.ALF;
import com.mashape.galileo.agent.modal.Entry;
import com.mashape.galileo.agent.network.AnalyticsBatchRequest;
import com.mashape.galileo.agent.network.AnalyticsConnKeepAliveStrategy;
import com.mashape.galileo.agent.network.AnalyticsRetryHandler;

/***
 * 
 * Analytics Configuration class
 *
 */
public class AnalyticsConfiguration {

	private final String environment;
	private final String analyticsToken;
	private boolean isAnlayticsEnabled;
	private String sendBody;
	private BlockingQueue<ALF> alfQueue;
	private int alfsBatchSize;
	private int alfsQueueSize;
	private int flushInterval;
	private int keepAliveTime;
	private URI analyticsURI;
	private ExecutorService workers;
	private ScheduledExecutorService scheduledService;
	private FutureRequestExecutionService requestExecService;
	private static AtomicLong totalAlfsSize = new AtomicLong();
	private int batchedALFAllowedSize;
	private int singleALFAllowedSize;
	private Gson gson = new Gson();

	private static AnalyticsConfiguration config;

	final static Logger LOGGER = Logger.getLogger(AnalyticsConfiguration.class);

	public static class Builder {

		private String analyticsServerHost = DEFAULT_ANALYTICS_HOST;
		private String analyticsServerPath = DEFAULT_ANALYTICS_SERVER_PATH;
		private boolean useHttps = DEFAULT_USE_HTTPS;
		private URI analyticsURI;
		private String environment = "";
		private String analyticsServerPort = DEFAULT_ANALYTICS_SERVER_PORT;
		private String analyticsToken;
		private boolean isAnlayticsEnabled = false;
		private String sendBody = DEFAULT_SEND_BODY;
		private int alfsBatchSize = DEFAULT_ANALYTICS_BATCH_SIZE;
		private int alfsQueueSize = DEFAULT_ANALYTICS_QUEUE_SIZE;
		private int connectionCount = DEFAULT_CONNECTION_COUNT;
		private int workerCount = DEFAULT_WORKER_COUNT_MAX;
		private int flushInterval = DEFAULT_ANALYTICS_FLUSH_INTERVAL;
		private int keepAliveTime = DEFAULT_CONNECTION_KEEP_ALIVE_TIME;
		private int batchedALFAllowedSize = DEFAULT_BATCHED_ALF_ALLOWED_SIZE_MB;
		private int singleALFAllowedSize = DEFAULT_SINGLE_ALF_ALLOWED_SIZE_MB;
		private BlockingQueue<ALF> alfQueue;
		private ExecutorService workers;
		private FutureRequestExecutionService requestExecService;
		private ScheduledExecutorService scheduledService;
		private boolean showPoolStatusTicker = false;
		private int tickerInterval = DEFAULT_TICKER_INTERVAL_TIME;
		private int retryCount = DEFAULT_ANALYTICS_RETRY_COUNT;

		public AnalyticsConfiguration build() {
			if (!this.isAnlayticsEnabled) {
				LOGGER.info("Galileo Agent is disabled");
			} else if (!PropertyUtil.notBlank(this.analyticsToken)) {
				isAnlayticsEnabled = false;
				LOGGER.error("analytics Token not set");
			} else {
				try {
					String scheme = (useHttps) ? "https" : "http";
					this.analyticsURI = new URIBuilder().setScheme(scheme).setHost(analyticsServerHost).setPort(Integer.parseInt(analyticsServerPort)).setPath(analyticsServerPath).build();
				} catch (Exception e1) {
					isAnlayticsEnabled = false;
					LOGGER.error("failed to create Galileo URI", e1);
					return (config = new AnalyticsConfiguration(this));
				}
				alfsQueueSize = 2 * alfsBatchSize;
				alfQueue = new ArrayBlockingQueue<ALF>(alfsQueueSize);
				HttpClient httpclient = HttpClientBuilder.create().setMaxConnPerRoute(connectionCount).setMaxConnTotal(connectionCount).setKeepAliveStrategy(new AnalyticsConnKeepAliveStrategy(keepAliveTime)).setRetryHandler(new AnalyticsRetryHandler(retryCount)).build();
				workers = Executors.newFixedThreadPool(workerCount);
				requestExecService = new FutureRequestExecutionService(httpclient, workers);
				logConfig();
				startTicker();
			}
			return (config = new AnalyticsConfiguration(this));
		}

		public Builder analyticsServerHost(String analyticsServerUrl) {
			if (PropertyUtil.notBlank(analyticsServerUrl)) {
				this.analyticsServerHost = analyticsServerUrl;
			}
			return this;
		}

		public Builder analyticsServerpatht(String analyticsServerPath) {
			if (PropertyUtil.notBlank(analyticsServerPath)) {
				this.analyticsServerPath = analyticsServerPath;
			}
			return this;
		}

		public Builder environment(String environment) {
			if (PropertyUtil.notBlank(environment)) {
				this.environment = environment;
			}
			return this;
		}

		public Builder analyticsServerPort(String analyticsServerPort) {
			if (PropertyUtil.notBlank(analyticsServerPort)) {
				this.analyticsServerPort = analyticsServerPort;
			}
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

		public Builder useHttps(String useHttps) {
			this.useHttps = Boolean.parseBoolean(useHttps);
			return this;
		}

		public Builder sendBody(String sendBody) {
			if (PropertyUtil.notBlank(sendBody))
				this.sendBody = sendBody;
			return this;
		}

		public Builder connectionCount(String connectionCount) {
			this.connectionCount = PropertyUtil.getEnvVarOrDefault(connectionCount, DEFAULT_CONNECTION_COUNT, 1, Integer.MAX_VALUE);
			return this;
		}

		public Builder alfsBatchSize(String workerCountMin) {
			this.alfsBatchSize = PropertyUtil.getEnvVarOrDefault(workerCountMin, DEFAULT_ANALYTICS_BATCH_SIZE, 1, MAX_ANALYTICS_BATCH_SIZE);
			return this;
		}

		public Builder retryCount(String retryCount) {
			this.retryCount = PropertyUtil.getEnvVarOrDefault(retryCount, DEFAULT_ANALYTICS_RETRY_COUNT, MAX_ANALYTICS_RETRY_COUNT);
			return this;
		}

		public Builder alfsQueueSize(String alfsQueueSize) {
			this.alfsQueueSize = PropertyUtil.getEnvVarOrDefault(alfsQueueSize, DEFAULT_ANALYTICS_QUEUE_SIZE, 1, Integer.MAX_VALUE);
			return this;
		}

		public Builder workerCount(String workerCount) {
			this.workerCount = PropertyUtil.getEnvVarOrDefault(workerCount, DEFAULT_WORKER_COUNT_MAX, 1, Integer.MAX_VALUE);
			return this;
		}

		public Builder flushInterval(String flushInterval) {
			this.flushInterval = PropertyUtil.getEnvVarOrDefault(flushInterval, DEFAULT_ANALYTICS_FLUSH_INTERVAL, MAX_ANALYTICS_FLUSH_INTERVAL);
			return this;
		}

		public Builder keepAliveTime(String keepAliveTime) {
			this.keepAliveTime = PropertyUtil.getEnvVarOrDefault(keepAliveTime, DEFAULT_CONNECTION_KEEP_ALIVE_TIME, MAX_CONNECTION_KEEP_ALIVE_TIME);
			return this;
		}

		public Builder showPoolStatusTicker(String showPoolStatusTicker) {
			this.showPoolStatusTicker = Boolean.parseBoolean(showPoolStatusTicker);
			return this;
		}

		public Builder tickerInterval(String tickerInterval) {
			this.tickerInterval = PropertyUtil.getEnvVarOrDefault(tickerInterval, DEFAULT_TICKER_INTERVAL_TIME);
			return this;
		}

		public Builder batchedALFAllowedSize(String batchedALFAllowedSize) {
			this.batchedALFAllowedSize = PropertyUtil.getEnvVarOrDefault(batchedALFAllowedSize, DEFAULT_BATCHED_ALF_ALLOWED_SIZE_MB, 1, Integer.MAX_VALUE);
			return this;
		}

		public Builder singleALFAllowedSize(String singleALFAllowedSize) {
			this.singleALFAllowedSize = PropertyUtil.getEnvVarOrDefault(singleALFAllowedSize, DEFAULT_SINGLE_ALF_ALLOWED_SIZE_MB, 1, Integer.MAX_VALUE);
			return this;
		}

		private void startTicker() {
			if (showPoolStatusTicker) {
				this.scheduledService = Executors.newScheduledThreadPool(1);
				scheduledService.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						LOGGER.info(requestExecService.metrics().toString());
					}
				}, 0, tickerInterval, TimeUnit.SECONDS);
			}
		}

		private void logConfig() {
			LOGGER.info("**********Galilleo Agent configuration************");
			LOGGER.info("host: " + analyticsServerHost);
			LOGGER.info("port: " + analyticsServerPort);
			LOGGER.info("protocol: " + ((useHttps) ? "https" : "http"));
			LOGGER.info("connection count: " + connectionCount);
			LOGGER.info("worker count: " + workerCount);
			LOGGER.info("connection keep alive time: " + keepAliveTime + " seconds");
			LOGGER.info("retry count: " + retryCount);
			LOGGER.info("count of ALFs per batch to be flushed: " + alfsBatchSize);
			LOGGER.info("size limit of ALFs per batch to be flushed: " + batchedALFAllowedSize + " MB");
			LOGGER.info("ALFs batch flush interval: " + flushInterval + " seconds");
			LOGGER.info("environment: " + environment);
			LOGGER.info("log Connection pool status: " + showPoolStatusTicker);
			LOGGER.info("allowed body size per request/response: " + singleALFAllowedSize + " MB");
			LOGGER.info("log request/response body: " + sendBody);
		}
	}

	private AnalyticsConfiguration(Builder builder) {
		this.environment = builder.environment;
		this.analyticsToken = builder.analyticsToken;
		this.isAnlayticsEnabled = builder.isAnlayticsEnabled;
		this.sendBody = builder.sendBody;
		this.workers = builder.workers;
		this.requestExecService = builder.requestExecService;
		this.alfQueue = builder.alfQueue;
		this.scheduledService = builder.scheduledService;
		this.analyticsURI = builder.analyticsURI;
		this.alfsBatchSize = builder.alfsBatchSize;
		this.alfsQueueSize = builder.alfsQueueSize;
		this.keepAliveTime = builder.keepAliveTime;
		this.flushInterval = builder.flushInterval;
		this.batchedALFAllowedSize = builder.batchedALFAllowedSize;
		this.singleALFAllowedSize = builder.singleALFAllowedSize;
	}

	public void enque(ALF alf) {
		if (alfQueue.size() >= alfsBatchSize || totalAlfsSize.get() >= batchedALFAllowedSize * 1000000) {
			List<ALF> alfs = new ArrayList<ALF>();
			alfQueue.drainTo(alfs);
			if (!alfs.isEmpty()) {
				workers.execute(new AnalyticsBatchRequest(config, alfs));
				LOGGER.debug("ALF queue reached thresold, flushing all alfs");
			}
			totalAlfsSize.getAndSet(0);
		}
		String alfString = new Gson().toJson(alf);
		Entry alfEntry = alf.getHar().getLog().getEntries().get(0);
		long singleALFAllowedSizeBytes = singleALFAllowedSize * 1000000;
		if (alfEntry.getRequest().getBodySize() > singleALFAllowedSizeBytes || alfEntry.getResponse().getBodySize() > singleALFAllowedSizeBytes) {
			LOGGER.warn("ALF size greater than allowed size, dropping ALF");
			return;
		}
		totalAlfsSize.addAndGet(alfString.getBytes().length);
		alfQueue.offer(alf);
	}

	public void drainTo(List<ALF> alfs) {
		alfQueue.drainTo(alfs);
	}

	public URI getAnalyticsServerURI() {
		return analyticsURI;
	}

	public String getEnvironment() {
		return environment;
	}

	public String getAnalyticsToken() {
		return analyticsToken;
	}

	public boolean isAnlayticsEnabled() {
		return isAnlayticsEnabled;
	}

	public String sendBody() {
		return sendBody;
	}

	public int getAlfQueueSize() {
		return alfsQueueSize;
	}

	public int getAlfBatchSize() {
		return alfsBatchSize;
	}

	public FutureRequestExecutionService getFutureRequestExecutor() {
		return requestExecService;
	}

	public ExecutorService getWorkers() {
		return workers;
	}

	public int getFlushInterval() {
		return flushInterval;
	}

	public int getKeepAliveTime() {
		return keepAliveTime;
	}

	public Gson getGson() {
		return gson;
	}

	public void shutdown() {
		try {
			if (alfQueue != null) {
				alfQueue.clear();
			}
			if (scheduledService != null) {
				scheduledService.shutdownNow();
			}
			if (requestExecService != null) {
				requestExecService.close();
			}
			if (workers != null) {
				workers.shutdownNow();
			}
		} catch (Exception e) {
			LOGGER.error("workers shutdown iterrupted", e);
		}
	}

	public static AnalyticsConfiguration getConfig() {
		if (config == null) {
			throw new RuntimeException("Galileo configuration missing");
		}
		return config;
	}

	public static long getTotalAlfsize() {
		return totalAlfsSize.get();
	}

	public static long resetTotalAlfsize() {
		return totalAlfsSize.getAndSet(0);
	}
}
