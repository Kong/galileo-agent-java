package com.mashape.analytics.agent.filter;

import java.io.IOException;
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

import com.google.gson.JsonObject;
import com.mashape.analytics.agent.connection.client.ConnectionManager;
import com.mashape.analytics.agent.mapper.AnalyticsDataMapper;
import com.mashape.analytics.agent.modal.Message;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;

public class AnalyticsFilter implements Filter {

	public static final String ANALYTICS_SERVER_URL = "analyticsServerUrl";
	public static final String ANALYTICS_SERVER_PORT = "analyticsServerPort";
	private ExecutorService analyticsServicexeExecutor;
	private int poolSize;
	FilterConfig config;
	private String analyticsServerUrl;
	private String analyticsServerPort;
	private String analyticsKey;
	private String formatVersion;
	private String agentVersion;
	

	@Override
	public void destroy() {
		analyticsServicexeExecutor.shutdown();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		RequestInterceptorWrapper request = new RequestInterceptorWrapper((HttpServletRequest) req);
		HttpServletResponse response = (HttpServletResponse) res;

		long startTime = System.currentTimeMillis();
		chain.doFilter(request, response);
		long endTime = System.currentTimeMillis();
		long timeElapsed = endTime - startTime;
		
		request.startAsync();
		analyticsServicexeExecutor.execute(new Runnable() {

			@Override
			public void run() {
				Map<String, String> messageProperties= new HashMap<String, String>();
				messageProperties.put(ANALYTICS_SERVER_URL, analyticsServerUrl);
				messageProperties.put(ANALYTICS_SERVER_PORT, analyticsServerPort);
				AnalyticsDataMapper mapper = new AnalyticsDataMapper(request, response, config);
				Message analyticsData = mapper.getAnalyticsData();
				ConnectionManager.sendMessage(messageProperties);
			}
		});
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		poolSize = Integer.parseInt(config.getInitParameter("poolSize"));
		analyticsServicexeExecutor = Executors.newFixedThreadPool(poolSize);
		analyticsServerUrl = config.getInitParameter("analyticsServerUrl");
		analyticsServerPort = config.getInitParameter("analyticsServerPort");
		analyticsKey = config.getInitParameter("analyticsKey");
	}

}
