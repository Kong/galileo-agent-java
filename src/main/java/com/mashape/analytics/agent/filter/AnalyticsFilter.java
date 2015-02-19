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
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.mashape.analytics.agent.connection.pool.Messenger;
import com.mashape.analytics.agent.connection.pool.ObjectPool;
import com.mashape.analytics.agent.connection.pool.SendAnalyticsTask;
import com.mashape.analytics.agent.connection.pool.Work;
import com.mashape.analytics.agent.mapper.AnalyticsDataMapper;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.analytics.agent.wrapper.ResponseInterceptorWrapper;

/**
 * 
 * @author Shashi
 * 
 *         AnalyticsFilter is a custom filter designed to intercept http request
 *         and response and send compiled data to Mashape analytics server.
 *
 */

@WebFilter(urlPatterns = "/*", initParams = {
		@WebInitParam(name = ANALYTICS_SERVER_URL, value = "socket.apianalytics.com"),
		@WebInitParam(name = ANALYTICS_SERVER_PORT, value = "5000") })
public class AnalyticsFilter implements Filter {

	final static Logger logger = Logger.getLogger(AnalyticsFilter.class);

	private ExecutorService analyticsServicexeExecutor;
	private String analyticsServerUrl;
	private String analyticsServerPort;
	private ObjectPool<Work> pool;

	@Override
	public void destroy() {
		analyticsServicexeExecutor.shutdown();
		pool.terminate();
	}

	/**
	 * Wraps the request and response for future read , chain the request and
	 * finally send the compiled data in HAR format to Analytics server
	 * 
	 * @see RequestInterceptorWrapper
	 * @see ResponseInterceptorWrapper
	 */
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
		callAsyncAnalytics(requestReceivedTime, request, response,
				waitStartTime - sendStartTime, waitEndTime - waitStartTime);
	}

	/**
	 * A pool of of thread handles the data transfer to Analytics server
	 * 
	 * @param requestReceivedTime
	 *            Date/Time when request received
	 * @param request
	 *            Http request intercepted by the filter
	 * @param response
	 *            Http response intercepted by the filter
	 * @param sendTime
	 *            Time taken by filter to send the intercepted request to next
	 *            sevlet/filter in chain
	 * @param waitTime
	 *            Wait time before receiving the response
	 * 
	 * @see AnalyticsDataMapper
	 * @see SendAnalyticsTask
	 */
	private void callAsyncAnalytics(Date requestReceivedTime,
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
			analyticsServicexeExecutor.execute(new SendAnalyticsTask(pool,
					messageProperties));
		} catch (Throwable x) {
			logger.error("Failed to send analytics data", x);
		}
	}

	/**
	 * Thread pools and socket pools are created
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		int poolSize = getEnvVarOrDefault(WORKER_COUNT, Runtime.getRuntime()
				.availableProcessors() * 2);
		int socketPoolMin = getEnvVarOrDefault(SOCKET_POOL_SIZE_MIN, 10);
		int socketPoolMax = getEnvVarOrDefault(SOCKET_POOL_SIZE_MAX, 20);
		int poolUpdateInterval = getEnvVarOrDefault(
				SOCKET_POOL_UPDATE_INTERVAL, 5);

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

	private int getEnvVarOrDefault(String name, int defaultVal) {
		String val = System.getenv(name);
		if (val != null && val.length() > 0) {
			return Integer.parseInt(val);
		}
		return defaultVal;
	}

}
