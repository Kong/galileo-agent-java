package com.mashape.analytics.agent.filter;

import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_DATA;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_URL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_SIZE_MAX;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_SIZE_MIN;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_UPDATE_INTERVAL;
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
import com.mashape.analytics.agent.connection.pool.Messenger;
import com.mashape.analytics.agent.connection.pool.ObjectPool;
import com.mashape.analytics.agent.connection.pool.Task;
import com.mashape.analytics.agent.connection.pool.Work;
import com.mashape.analytics.agent.mapper.AnalyticsDataMapper;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.modal.Message;
import com.mashape.analytics.agent.modal.Timings;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.analytics.agent.wrapper.ResponseInterceptorWrapper;

public class AnalyticsFilter implements Filter {

	final static Logger logger = Logger.getLogger(AnalyticsFilter.class);

	private ExecutorService analyticsServicexeExecutor;
	private FilterConfig config;
	private String analyticsServerUrl;
	private String analyticsServerPort;
	private ObjectPool<Work> pool;

	@Override
	public void destroy() {
		analyticsServicexeExecutor.shutdown();
		pool.terminate();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		long sendStartTime = System.currentTimeMillis();
		Date requestReceivedTime = new Date();
		RequestInterceptorWrapper request = new RequestInterceptorWrapper(
				(HttpServletRequest) req);
		ResponseInterceptorWrapper response = new ResponseInterceptorWrapper(
				(HttpServletResponse) res);
		long waitStartTime = System.currentTimeMillis();
		chain.doFilter(request, response);
		long waitEndTime = System.currentTimeMillis();
		callAnalytics(requestReceivedTime, request, response, waitStartTime
				- sendStartTime, waitEndTime - waitStartTime);
	}

	private void callAnalytics(Date requestReceivedTime,
			RequestInterceptorWrapper request,
			ResponseInterceptorWrapper response, long sendTime, long waitTime) {
		try {
			long recvStartTime = System.currentTimeMillis();
			Map<String, Object> messageProperties = new HashMap<String, Object>();
			messageProperties.put(ANALYTICS_SERVER_URL, analyticsServerUrl);
			messageProperties.put(ANALYTICS_SERVER_PORT, analyticsServerPort);
			AnalyticsDataMapper mapper = new AnalyticsDataMapper(request,
					response);
			long recvEndTime = System.currentTimeMillis();
			Entry analyticsData = mapper.getAnalyticsData(requestReceivedTime,
					sendTime, waitTime);
			analyticsData.getTimings().setReceive(recvEndTime - recvStartTime);
			messageProperties.put(ANALYTICS_DATA, analyticsData);
			analyticsServicexeExecutor
					.execute(new Task(pool, messageProperties));
		} catch (Throwable x) {
			logger.error("Failed to send analytics data", x);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		int poolSize = Integer.parseInt(config.getInitParameter(WORKER_COUNT));
		int socketPoolMin = Integer.parseInt(config
				.getInitParameter(SOCKET_POOL_SIZE_MIN));
		int socketPoolMax = Integer.parseInt(config
				.getInitParameter(SOCKET_POOL_SIZE_MAX));
		int poolUpdateInterval = Integer.parseInt(config
				.getInitParameter(SOCKET_POOL_UPDATE_INTERVAL));

		analyticsServerUrl = config.getInitParameter(ANALYTICS_SERVER_URL);
		analyticsServerPort = config.getInitParameter(ANALYTICS_SERVER_PORT);
		pool = new ObjectPool<Work>(socketPoolMin, socketPoolMax,
				poolUpdateInterval) {
			@Override
			public Work createPoolObject() {
				return new Messenger();
			}
		};
		analyticsServicexeExecutor = Executors.newFixedThreadPool(poolSize);
	}

}
