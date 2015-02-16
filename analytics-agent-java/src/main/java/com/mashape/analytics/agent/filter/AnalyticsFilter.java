package com.mashape.analytics.agent.filter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.mashape.analytics.agent.connection.client.ConnectionManager;
import com.mashape.analytics.agent.mapper.AnalyticsDataMapper;
import com.mashape.analytics.agent.modal.Message;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.analytics.agent.wrapper.ResponseInterceptorWrapper;

public class AnalyticsFilter implements Filter {

	public static final String ANALYTICS_SERVER_URL = "analyticsServerUrl";
	public static final String ANALYTICS_SERVER_PORT = "analyticsServerPort";
	protected static final String ANALYTICS_DATA = "data";
	private ExecutorService analyticsServicexeExecutor;
	private int poolSize;
	FilterConfig config;
	private String analyticsServerUrl;
	private String analyticsServerPort;

	@Override
	public void destroy() {
		// analyticsServicexeExecutor.shutdown();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		Date requestReceivedTime = new Date();
		System.out.println(((HttpServletRequest) req).getMethod());
		RequestInterceptorWrapper request = new RequestInterceptorWrapper(
				(HttpServletRequest) req);
		System.out.println(request.getMethod());
		ResponseInterceptorWrapper response = new ResponseInterceptorWrapper(
				(HttpServletResponse) res);
		long startTime = System.currentTimeMillis();
		chain.doFilter(request, response);
		long endTime = System.currentTimeMillis();
		AsyncContext ac = request.startAsync(request, response);
		ac.setTimeout(90000000000L);
		ac.start(new Runnable() {
			@Override
			public void run() {
				try {

					Map<String, String> messageProperties = new HashMap<String, String>();
					messageProperties.put(ANALYTICS_SERVER_URL,
							analyticsServerUrl);
					messageProperties.put(ANALYTICS_SERVER_PORT,
							analyticsServerPort);
					AnalyticsDataMapper mapper = new AnalyticsDataMapper(ac
							.getRequest(), ac.getResponse(), config);
					Message analyticsData = mapper.getAnalyticsData(
							requestReceivedTime, startTime, endTime);
					String data = new Gson().toJson(analyticsData);
					messageProperties.put(ANALYTICS_DATA, data);
					System.out.println(data);
					ConnectionManager.sendMessage(messageProperties);
				} catch (Throwable x) {
					// suppress
				} finally {
					ac.complete();
				}

			}
		});
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		poolSize = Integer.parseInt(config.getInitParameter("poolSize"));
		// analyticsServicexeExecutor = Executors.newFixedThreadPool(poolSize);
		analyticsServerUrl = config.getInitParameter("analyticsServerUrl");
		analyticsServerPort = config.getInitParameter("analyticsServerPort");
	}

}
