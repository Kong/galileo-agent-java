package com.mashape.galileo.agent.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import com.mashape.galileo.agent.config.AnalyticsConfiguration;
import com.mashape.galileo.agent.modal.ALF;

public class AnalyticsDataFlusher implements Runnable {
	private static Logger LOGGER = Logger.getLogger(AnalyticsBatchRequest.class);
	private AnalyticsConfiguration config;

	public AnalyticsDataFlusher(AnalyticsConfiguration config) {
		this.config = config;
	}

	@Override
	public void run() {
		try {
			List<ALF> alfs = new ArrayList<ALF>();
			config.drainTo(alfs);
			new AnalyticsBatchRequest(config, alfs).run();;
		} catch (Exception e) {
			LOGGER.error("failed to send data, dropping data", e);
		}
	}
}
