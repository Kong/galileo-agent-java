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

package com.mashape.galileo.agent.filter;

import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_BODY_SIZE_MB;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_BATCH_SIZE;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_BATCH_SIZE_MB;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_CONNECTION_COUNT;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_ENABLED;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_ENVIRONMENT;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_FLUSH_INTERVAL;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_HOST;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_PORT;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_RETRY_COUNT;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_TOKEN;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_USE_HTTPS;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ANALYTICS_WORKER_COUNT;
import static com.mashape.galileo.agent.common.AnalyticsConstants.CONNECTION_KEEPALIVE_TIME;
import static com.mashape.galileo.agent.common.AnalyticsConstants.SEND_BODY;
import static com.mashape.galileo.agent.common.AnalyticsConstants.SHOW_STATUS_TICKER;
import static com.mashape.galileo.agent.common.AnalyticsConstants.STATUS_TICKER_INTERVAL;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.mashape.galileo.agent.config.AnalyticsConfiguration;
import com.mashape.galileo.agent.mapper.AnalyticsDataMapper;
import com.mashape.galileo.agent.modal.ALF;
import com.mashape.galileo.agent.modal.Entry;
import com.mashape.galileo.agent.network.AnalyticsBatchRequest;
import com.mashape.galileo.agent.network.AnalyticsDataFlusher;
import com.mashape.galileo.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.galileo.agent.wrapper.ResponseInterceptorWrapper;

/**
 * AnalyticsFilter is a custom filter designed to intercept http request and
 * response and send compiled data to Mashape analytics server.
 *
 */

public class AnalyticsFilter implements Filter {

	final static Logger LOGGER = Logger.getLogger(AnalyticsFilter.class);
	private AnalyticsConfiguration analyticsConfiguration;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@Override
	public void destroy() {
		if (scheduler != null)
			scheduler.shutdown();
		if (analyticsConfiguration != null)
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
			response.finishResponse();
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
			ALF alf = new AnalyticsDataMapper(request, response, analyticsConfiguration).getAnalyticsData(requestReceivedTime, sendTime, waitTime);
			alf.setServiceToken(AnalyticsConfiguration.getConfig().getAnalyticsToken());
			alf.setEnvironment(AnalyticsConfiguration.getConfig().getEnvironment());
			long recvEndTime = System.currentTimeMillis();
			Entry entry = alf.getHar().getLog().getEntries().get(0);
			entry.getTimings().setReceive(recvEndTime - recvStartTime);
			entry.setTime((recvEndTime - recvStartTime) + sendTime + waitTime);
			analyticsConfiguration.enque(alf);
		} catch (Throwable x) {
			LOGGER.error("failed to send Analytics data", x);
		}
	}

	/**
	 * Analytics configuration initialization
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		analyticsConfiguration = new AnalyticsConfiguration.Builder()
		.isAnlayticsEnabled(System.getProperty(ANALYTICS_ENABLED))
		.analyticsServerHost(config.getInitParameter(ANALYTICS_HOST))
		.analyticsServerPort(config.getInitParameter(ANALYTICS_PORT))
		.useHttps(System.getProperty(ANALYTICS_USE_HTTPS))
		.analyticsToken(System.getProperty(ANALYTICS_TOKEN))
		.sendBody(System.getProperty(SEND_BODY))
		.environment(System.getProperty(ANALYTICS_ENVIRONMENT))
		.workerCount(System.getProperty(ANALYTICS_WORKER_COUNT))
		.connectionCount(System.getProperty(ANALYTICS_CONNECTION_COUNT))
		.flushInterval(System.getProperty(ANALYTICS_FLUSH_INTERVAL))
		.alfsBatchSize(System.getProperty(ANALYTICS_BATCH_SIZE))
		.showPoolStatusTicker(System.getProperty(SHOW_STATUS_TICKER))
		.tickerInterval(System.getProperty(STATUS_TICKER_INTERVAL))
		.retryCount(System.getProperty(ANALYTICS_RETRY_COUNT))
		.keepAliveTime(System.getProperty(CONNECTION_KEEPALIVE_TIME))
		.batchedALFAllowedSize(System.getProperty(ANALYTICS_BATCH_SIZE_MB))
		.singleALFAllowedSize(System.getProperty(ANALYTICS_BODY_SIZE_MB))
		.build();
		
		if(analyticsConfiguration.isAnlayticsEnabled()){
			scheduler.scheduleAtFixedRate(new AnalyticsDataFlusher(analyticsConfiguration), 0, analyticsConfiguration.getFlushInterval(), TimeUnit.SECONDS);
		}
	}
}
