package com.mashape.analytics.agent.filter;

import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_DATA;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_URL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.WORKER_COUNT;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.mashape.analytics.agent.connection.ConnectionManager;
import com.mashape.analytics.agent.mapper.AnalyticsDataMapper;
import com.mashape.analytics.agent.modal.Message;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.analytics.agent.wrapper.ResponseInterceptorWrapper;

public class AnalyticsFilter implements Filter {

	final static Logger logger = Logger.getLogger(AnalyticsFilter.class);

	private ExecutorService analyticsServicexeExecutor;
	private FilterConfig config;
	private String analyticsServerUrl;
	private String analyticsServerPort;

	@Override
	public void destroy() {
		analyticsServicexeExecutor.shutdown();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		Date requestReceivedTime = new Date();
		RequestInterceptorWrapper request = new RequestInterceptorWrapper(
				(HttpServletRequest) req);
		ResponseInterceptorWrapper response = new ResponseInterceptorWrapper(
				(HttpServletResponse) res);
		long startTime = System.currentTimeMillis();
		chain.doFilter(request, response);
		long endTime = System.currentTimeMillis();
		callAnalytics(requestReceivedTime, request, response, startTime,
				endTime);
	}

	private void callAnalytics(Date requestReceivedTime,
			RequestInterceptorWrapper request,
			ResponseInterceptorWrapper response, long startTime, long endTime) {
		try {
			Map<String, String> messageProperties = new HashMap<String, String>();
			messageProperties.put(ANALYTICS_SERVER_URL, analyticsServerUrl);
			messageProperties.put(ANALYTICS_SERVER_PORT, analyticsServerPort);
			AnalyticsDataMapper mapper = new AnalyticsDataMapper(request,
					response, config);
			Message analyticsData = mapper.getAnalyticsData(
					requestReceivedTime, startTime, endTime);
			String data = new Gson().toJson(analyticsData);
			logger.debug(data);
			messageProperties.put(ANALYTICS_DATA, data);
			analyticsServicexeExecutor.execute(new Runnable() {
				@Override
				public void run() {
					ConnectionManager.sendMessage(messageProperties);
				}
			});
		} catch (Throwable x) {
			logger.error("Failed to send analytics data", x);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		int poolSize = Integer.parseInt(config.getInitParameter(WORKER_COUNT));
		analyticsServerUrl = config.getInitParameter(ANALYTICS_SERVER_URL);
		analyticsServerPort = config.getInitParameter(ANALYTICS_SERVER_PORT);
		analyticsServicexeExecutor = Executors.newFixedThreadPool(poolSize);
	}

}
