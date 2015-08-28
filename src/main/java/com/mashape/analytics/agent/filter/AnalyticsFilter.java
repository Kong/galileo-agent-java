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
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_ENABLED;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_URL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_TOKEN;
import static com.mashape.analytics.agent.common.AnalyticsConstants.CLIENT_IP_ADDRESS;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ENVIRONMENT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SHOW_POOL_STATUS_TICKER;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_SIZE_MAX;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_SIZE_MIN;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_UPDATE_INTERVAL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.STATUS_TICKER_INTERVAL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.WORKER_QUEUE_COUNT;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.mashape.analytics.agent.connection.pool.AnalyticsConfiguration;
import com.mashape.analytics.agent.connection.pool.SendAnalyticsTask;
import com.mashape.analytics.agent.mapper.AnalyticsDataMapper;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.analytics.agent.wrapper.ResponseInterceptorWrapper;

/**
 * AnalyticsFilter is a custom filter designed to intercept http request and
 * response and send compiled data to Mashape analytics server.
 *
 */

public class AnalyticsFilter implements Filter {

	final static Logger LOGGER = Logger.getLogger(AnalyticsFilter.class);
	private AnalyticsConfiguration analyticsConfiguration;

	@Override
	public void destroy() {
		analyticsConfiguration.shutdown();
	}

	/**
	 * Wraps the request and response for future read , chain the request and
	 * finally send the compiled data in HAR format to Analytics server
	 * 
	 * @see RequestInterceptorWrapper
	 * @see ResponseInterceptorWrapper
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (analyticsConfiguration.isAnlayticsEnabled()) {
			long sendStartTime = System.currentTimeMillis();
			Date requestReceivedTime = new Date();
			RequestInterceptorWrapper request = new RequestInterceptorWrapper((HttpServletRequest) req);
			ResponseInterceptorWrapper response = new ResponseInterceptorWrapper((HttpServletResponse) res);
			long waitStartTime = System.currentTimeMillis();
			chain.doFilter(request, response);
			long waitEndTime = System.currentTimeMillis();
			callAsyncAnalytics(requestReceivedTime, request, response, waitStartTime - sendStartTime, waitEndTime - waitStartTime);
		} else {
			chain.doFilter(req, res);
		}
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
	private void callAsyncAnalytics(Date requestReceivedTime, RequestInterceptorWrapper request, ResponseInterceptorWrapper response, long sendTime,
			long waitTime) {
		try {
			long recvStartTime = System.currentTimeMillis();
			Map<String, Object> messageProperties = new HashMap<String, Object>();
			Entry analyticsData = new AnalyticsDataMapper(request, response).getAnalyticsData(requestReceivedTime, sendTime, waitTime);
			long recvEndTime = System.currentTimeMillis();
			analyticsData.getTimings().setReceive(recvEndTime - recvStartTime);
			analyticsData.setTime((recvEndTime - recvStartTime) + sendTime + waitTime);
			messageProperties.put(ANALYTICS_DATA, analyticsData);
			messageProperties.put(CLIENT_IP_ADDRESS, request.getRemoteAddr());
			analyticsConfiguration.getWorkers().execute(new SendAnalyticsTask(messageProperties));
		} catch (Throwable x) {
			LOGGER.error("Failed to send Analytics data", x);
		}
	}

	/**
	 * Analytics configuration setup
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		analyticsConfiguration = new AnalyticsConfiguration.Builder()
			.isAnlayticsEnabled(System.getProperty(ANALYTICS_ENABLED))
			.analyticsServerUrl(config.getInitParameter(ANALYTICS_SERVER_URL))
			.analyticsServerPort(config.getInitParameter(ANALYTICS_SERVER_PORT))
			.analyticsToken(System.getProperty(ANALYTICS_TOKEN))
			.environment(System.getProperty(ENVIRONMENT))
			.workerCountMin(System.getProperty(SOCKET_POOL_SIZE_MIN))
			.workerCountMax(System.getProperty(SOCKET_POOL_SIZE_MAX))
			.workerKeepAliveTime(System.getProperty(SOCKET_POOL_UPDATE_INTERVAL))
			.taskQueueSize(System.getProperty(WORKER_QUEUE_COUNT))
			.showPoolStatusTicker(System.getProperty(SHOW_POOL_STATUS_TICKER))
			.tickerInterval(System.getProperty(STATUS_TICKER_INTERVAL))
			.build();
	}
}
