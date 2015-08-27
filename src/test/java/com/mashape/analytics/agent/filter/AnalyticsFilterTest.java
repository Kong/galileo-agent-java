package com.mashape.analytics.agent.filter;

import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_ENABLED;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_URL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_TOKEN;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ENVIRONMENT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_SIZE_MAX;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_SIZE_MIN;
import static com.mashape.analytics.agent.common.AnalyticsConstants.SOCKET_POOL_UPDATE_INTERVAL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.WORKER_QUEUE_COUNT;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import com.mashape.analytics.agent.connection.pool.Messenger;
import com.mashape.analytics.agent.connection.pool.Executor;
import com.mashape.analytics.agent.mapper.AnalyticsDataMapper;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.modal.Timings;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.analytics.agent.wrapper.ResponseInterceptorWrapper;

@RunWith(JMockit.class)
public class AnalyticsFilterTest {

	@Tested
	private AnalyticsFilter filter;

	@Mocked
	private FilterConfig config;

	@Mocked("doFilter")
	private FilterChain chain;

	@Mocked
	private ThreadPoolExecutor mokedExecutors;

	@Mocked
	private ExecutorService analyticsServicexeExecutor;

	@Mocked
	private ScheduledExecutorService scheduExecutorService;

	@Mocked("getProperty")
	private System mockedSystem;

	@Mocked("newScheduledThreadPool")
	private Executors executors;

	@Mocked
	private AnalyticsDataMapper mapper;

	private AtomicInteger val = new AtomicInteger(0);

	@Test
	public void test() throws IOException, ServletException {

		new NonStrictExpectations() {

			{
				System.getProperty(ANALYTICS_ENABLED);
				result = "true";
				config.getInitParameter(ANALYTICS_SERVER_URL);
				result = "analytics.com";
				config.getInitParameter(ANALYTICS_SERVER_PORT);
				result = "5000";
				System.getProperty(ANALYTICS_TOKEN);
				result = "abcedf";
				System.getProperty(WORKER_QUEUE_COUNT);
				result = "2";
				System.getProperty(SOCKET_POOL_SIZE_MIN);
				result = "5";
				System.getProperty(SOCKET_POOL_SIZE_MAX);
				result = "10";
				System.getProperty(SOCKET_POOL_UPDATE_INTERVAL);
				result = "5";
				System.getProperty(ENVIRONMENT);
				result = "TEST";
				chain.doFilter((RequestInterceptorWrapper) any, (ResponseInterceptorWrapper) any);
				new AnalyticsDataMapper((RequestInterceptorWrapper) any, (ResponseInterceptorWrapper) any).getAnalyticsData((Date) any, anyLong, anyLong);
				result = getEntry();
				Executors.newScheduledThreadPool(anyInt);
				result = scheduExecutorService;
				scheduExecutorService.scheduleAtFixedRate((Runnable) any, anyInt, anyInt, (TimeUnit) any);
				mokedExecutors.execute((Runnable) any);
			}
		};

		try {
			filter.init(config);
			filter.doFilter(req, res, chain);
		} catch (ServletException e) {
			fail("it should never fail");
		} catch (IOException e) {
			fail("it should never fail");
		}

	}

	@Test
	public void testExceptionSuppressed() throws IOException, ServletException {

		new NonStrictExpectations() {

			{
				System.getProperty(ANALYTICS_ENABLED);
				result = "true";
				config.getInitParameter(ANALYTICS_SERVER_URL);
				result = "analytics.com";
				config.getInitParameter(ANALYTICS_SERVER_PORT);
				result = "5000";
				System.getProperty(ANALYTICS_TOKEN);
				result = "abcedf";
				System.getProperty(WORKER_QUEUE_COUNT);
				result = "2";
				System.getProperty(SOCKET_POOL_SIZE_MIN);
				result = "5";
				System.getProperty(SOCKET_POOL_SIZE_MAX);
				result = "10";
				System.getProperty(SOCKET_POOL_UPDATE_INTERVAL);
				result = "5";
				chain.doFilter((RequestInterceptorWrapper) any, (ResponseInterceptorWrapper) any);
				new AnalyticsDataMapper((RequestInterceptorWrapper) any, (ResponseInterceptorWrapper) any).getAnalyticsData((Date) any, anyLong, anyLong);
				result = new Throwable();
				Executors.newScheduledThreadPool(anyInt);
				result = scheduExecutorService;
				scheduExecutorService.scheduleAtFixedRate((Runnable) any, anyInt, anyInt, (TimeUnit) any);

			}
		};

		try {
			filter.init(config);
			filter.doFilter(req, res, chain);
		} catch (ServletException e) {
			fail("it should never fail");
		} catch (IOException e) {
			fail("it should never fail");
		}

	}

	@Test
	public void testNoEnvVarSet() throws IOException, ServletException {

		new NonStrictExpectations() {

			{
				System.getProperty(ANALYTICS_ENABLED);
				result = "true";
				config.getInitParameter(ANALYTICS_SERVER_URL);
				result = "analytics.com";
				config.getInitParameter(ANALYTICS_SERVER_PORT);
				result = "5000";
				System.getProperty(ANALYTICS_TOKEN);
				result = "abcedf";
				System.getProperty(WORKER_QUEUE_COUNT);
				result = null;
				System.getProperty(SOCKET_POOL_SIZE_MIN);
				result = null;
				System.getProperty(SOCKET_POOL_SIZE_MAX);
				result = null;
				System.getProperty(SOCKET_POOL_UPDATE_INTERVAL);
				result = null;
				System.getProperty(ENVIRONMENT);
				result = "TEST";
				chain.doFilter((RequestInterceptorWrapper) any, (ResponseInterceptorWrapper) any);
				new AnalyticsDataMapper((RequestInterceptorWrapper) any, (ResponseInterceptorWrapper) any).getAnalyticsData((Date) any, anyLong, anyLong);
				result = getEntry();
				Executors.newScheduledThreadPool(anyInt);
				result = scheduExecutorService;
				scheduExecutorService.scheduleAtFixedRate((Runnable) any, anyInt, anyInt, (TimeUnit) any);
				mokedExecutors.execute((Runnable) any);
			}
		};

		try {
			filter.init(config);
			filter.doFilter(req, res, chain);
		} catch (ServletException e) {
			fail("it should never fail");
		} catch (IOException e) {
			fail("it should never fail");
		}

	}

	@Test
	public void testAnalyticsDisabled() throws IOException, ServletException {

		new NonStrictExpectations() {

			{
				System.getProperty(ANALYTICS_ENABLED);
				result = null;
				chain.doFilter((RequestInterceptorWrapper) any, (ResponseInterceptorWrapper) any);
			}
		};

		try {
			filter.init(config);
			filter.doFilter(req, res, chain);
		} catch (ServletException e) {
			fail("it should never fail");
		} catch (IOException e) {
			fail("it should never fail");
		}

	}

	private Entry getEntry() {
		Entry entry = new Entry();
		entry.setTimings(new Timings());
		return entry;
	}

	private ServletRequest req = new HttpServletRequest() {

		@Override
		public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {

			return null;
		}

		@Override
		public AsyncContext startAsync() throws IllegalStateException {

			return null;
		}

		@Override
		public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {

		}

		@Override
		public void setAttribute(String arg0, Object arg1) {

		}

		@Override
		public void removeAttribute(String arg0) {

		}

		@Override
		public boolean isSecure() {

			return false;
		}

		@Override
		public boolean isAsyncSupported() {

			return false;
		}

		@Override
		public boolean isAsyncStarted() {

			return false;
		}

		@Override
		public ServletContext getServletContext() {

			return null;
		}

		@Override
		public int getServerPort() {

			return 0;
		}

		@Override
		public String getServerName() {

			return null;
		}

		@Override
		public String getScheme() {

			return null;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String arg0) {

			return null;
		}

		@Override
		public int getRemotePort() {

			return 0;
		}

		@Override
		public String getRemoteHost() {

			return null;
		}

		@Override
		public String getRemoteAddr() {

			return null;
		}

		@Override
		public String getRealPath(String arg0) {

			return null;
		}

		@Override
		public BufferedReader getReader() throws IOException {

			return null;
		}

		@Override
		public String getProtocol() {

			return null;
		}

		@Override
		public String[] getParameterValues(String arg0) {

			return null;
		}

		@Override
		public Enumeration<String> getParameterNames() {

			return null;
		}

		@Override
		public Map<String, String[]> getParameterMap() {

			return null;
		}

		@Override
		public String getParameter(String arg0) {

			return null;
		}

		@Override
		public Enumeration<Locale> getLocales() {

			return null;
		}

		@Override
		public Locale getLocale() {

			return null;
		}

		@Override
		public int getLocalPort() {

			return 0;
		}

		@Override
		public String getLocalName() {

			return null;
		}

		@Override
		public String getLocalAddr() {

			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {

			return null;
		}

		@Override
		public DispatcherType getDispatcherType() {

			return null;
		}

		@Override
		public String getContentType() {

			return null;
		}

		@Override
		public int getContentLength() {

			return 0;
		}

		@Override
		public String getCharacterEncoding() {

			return null;
		}

		@Override
		public Enumeration<String> getAttributeNames() {

			return null;
		}

		@Override
		public Object getAttribute(String arg0) {

			return null;
		}

		@Override
		public AsyncContext getAsyncContext() {

			return null;
		}

		@Override
		public void logout() throws ServletException {

		}

		@Override
		public void login(String arg0, String arg1) throws ServletException {

		}

		@Override
		public boolean isUserInRole(String arg0) {

			return false;
		}

		@Override
		public boolean isRequestedSessionIdValid() {

			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {

			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {

			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {

			return false;
		}

		@Override
		public Principal getUserPrincipal() {

			return null;
		}

		@Override
		public HttpSession getSession(boolean arg0) {

			return null;
		}

		@Override
		public HttpSession getSession() {

			return null;
		}

		@Override
		public String getServletPath() {

			return null;
		}

		@Override
		public String getRequestedSessionId() {

			return null;
		}

		@Override
		public StringBuffer getRequestURL() {

			return null;
		}

		@Override
		public String getRequestURI() {

			return null;
		}

		@Override
		public String getRemoteUser() {

			return null;
		}

		@Override
		public String getQueryString() {

			return null;
		}

		@Override
		public String getPathTranslated() {

			return null;
		}

		@Override
		public String getPathInfo() {

			return null;
		}

		@Override
		public Collection<Part> getParts() throws IOException, ServletException {

			return null;
		}

		@Override
		public Part getPart(String arg0) throws IOException, ServletException {

			return null;
		}

		@Override
		public String getMethod() {

			return null;
		}

		@Override
		public int getIntHeader(String arg0) {

			return 0;
		}

		@Override
		public Enumeration<String> getHeaders(String arg0) {

			return null;
		}

		@Override
		public Enumeration<String> getHeaderNames() {

			return null;
		}

		@Override
		public String getHeader(String arg0) {

			return null;
		}

		@Override
		public long getDateHeader(String arg0) {

			return 0;
		}

		@Override
		public Cookie[] getCookies() {

			return null;
		}

		@Override
		public String getContextPath() {

			return null;
		}

		@Override
		public String getAuthType() {

			return null;
		}

		@Override
		public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {

			return false;
		}
	};

	private ServletResponse res = new HttpServletResponse() {

		@Override
		public void setLocale(Locale arg0) {

		}

		@Override
		public void setContentType(String arg0) {

		}

		@Override
		public void setContentLength(int arg0) {

		}

		@Override
		public void setCharacterEncoding(String arg0) {

		}

		@Override
		public void setBufferSize(int arg0) {

		}

		@Override
		public void resetBuffer() {

		}

		@Override
		public void reset() {

		}

		@Override
		public boolean isCommitted() {

			return false;
		}

		@Override
		public PrintWriter getWriter() throws IOException {

			return null;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {

			return null;
		}

		@Override
		public Locale getLocale() {

			return null;
		}

		@Override
		public String getContentType() {

			return null;
		}

		@Override
		public String getCharacterEncoding() {

			return null;
		}

		@Override
		public int getBufferSize() {

			return 0;
		}

		@Override
		public void flushBuffer() throws IOException {

		}

		@Override
		public void setStatus(int arg0, String arg1) {

		}

		@Override
		public void setStatus(int arg0) {

		}

		@Override
		public void setIntHeader(String arg0, int arg1) {

		}

		@Override
		public void setHeader(String arg0, String arg1) {

		}

		@Override
		public void setDateHeader(String arg0, long arg1) {

		}

		@Override
		public void sendRedirect(String arg0) throws IOException {

		}

		@Override
		public void sendError(int arg0, String arg1) throws IOException {

		}

		@Override
		public void sendError(int arg0) throws IOException {

		}

		@Override
		public int getStatus() {

			return 0;
		}

		@Override
		public Collection<String> getHeaders(String arg0) {

			return null;
		}

		@Override
		public Collection<String> getHeaderNames() {

			return null;
		}

		@Override
		public String getHeader(String arg0) {

			return null;
		}

		@Override
		public String encodeUrl(String arg0) {

			return null;
		}

		@Override
		public String encodeURL(String arg0) {

			return null;
		}

		@Override
		public String encodeRedirectUrl(String arg0) {

			return null;
		}

		@Override
		public String encodeRedirectURL(String arg0) {

			return null;
		}

		@Override
		public boolean containsHeader(String arg0) {

			return false;
		}

		@Override
		public void addIntHeader(String arg0, int arg1) {

		}

		@Override
		public void addHeader(String arg0, String arg1) {

		}

		@Override
		public void addDateHeader(String arg0, long arg1) {

		}

		@Override
		public void addCookie(Cookie arg0) {

		}
	};

}
