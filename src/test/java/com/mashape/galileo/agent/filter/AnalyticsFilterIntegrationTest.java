package com.mashape.galileo.agent.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.DispatcherType;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mashape.galileo.agent.common.AnalyticsConstants;
import com.mashape.galileo.agent.modal.ALF;
import com.mashape.galileo.agent.modal.Entry;

public class AnalyticsFilterIntegrationTest {

	private static Server server;
	private static ExecutorService excecutor;
	public static Queue<ALF> globalQueue = new ConcurrentLinkedQueue<ALF>();

	@BeforeClass
	public static void setup() {
		excecutor = Executors.newFixedThreadPool(2);
		System.setProperty(AnalyticsConstants.ANALYTICS_TOKEN, "YOUR_SERVICE_TOKEN");
		System.setProperty(AnalyticsConstants.ANALYTICS_ENABLED, "true");
		System.setProperty(AnalyticsConstants.SEND_BODY, "all");
		startDummyCollector();
	}

	@Test
	public void test() throws Exception {
		Thread.sleep(2000);
		startJettyServer();
		Thread.sleep(2000);
		HttpPost httpPost = new HttpPost("http://localhost:5559");
		CloseableHttpClient httpclient = HttpClients.createDefault();
		httpPost.setEntity(new StringEntity("Test"));
		CloseableHttpResponse response = httpclient.execute(httpPost);
		response.close();
		while(globalQueue.isEmpty());
		ALF message = globalQueue.poll();
		assertNotNull(message);
		assertNotNull(message.getServiceToken());
		assertNotNull(message.getHar());
		assertNotNull(message.getVersion());
		assertNotNull(message.getHar().getLog());
		assertNotNull(message.getHar().getLog().getCreator());
		assertTrue(message.getHar().getLog().getEntries().size() > 0);
		Entry entry = message.getHar().getLog().getEntries().get(0);
		assertNotNull(entry);
		assertNotNull(entry.getStartedDateTime());
		assertNotNull(entry.getRequest());
		assertNotNull(entry.getResponse());
		assertNotNull(entry.getTimings());
		assertEquals("127.0.0.1", entry.getClientIPAddress());
		assertEquals("127.0.0.1", entry.getServerIPAddress());

		assertNotNull(entry.getRequest().getMethod());
		assertNotNull(entry.getRequest().getUrl());
		assertNotNull(entry.getRequest().getHttpVersion());
		assertNotNull(entry.getRequest().getHeaders());
		assertNotNull(entry.getRequest().getHeadersSize());
		assertNotNull(entry.getRequest().getBodySize());
		assertNotNull(entry.getRequest().getQueryString());

		assertNotNull(entry.getRequest().getPostData().getText());
		assertNotNull(entry.getRequest().getPostData().getEncoding());
		assertNotNull(entry.getRequest().getPostData().getMimeType());

		assertNotNull(entry.getResponse().getHttpVersion());
		assertNotNull(entry.getResponse().getHeaders());
		assertNotNull(entry.getResponse().getHeadersSize());
		assertNotNull(entry.getResponse().getContent());
		assertNotNull(entry.getResponse().getBodySize());

		assertNotNull(entry.getResponse().getContent().getText());
		assertNotNull(entry.getResponse().getContent().getEncoding());
		assertNotNull(entry.getResponse().getContent().getMimeType());
	}
	
	
	@Test
	public void testCount() throws Exception {
		globalQueue.clear();
		Thread.sleep(2000);
		startJettyServer();
		Thread.sleep(2000);
		HttpPost httpPost = new HttpPost("http://localhost:5559");
		CloseableHttpClient httpclient = HttpClients.createDefault();
		httpPost.setEntity(new StringEntity("Test"));
		int count = 10;
		Thread.sleep(5000);
		while(count > 0){
			CloseableHttpResponse response = httpclient.execute(httpPost);
			response.close();
			count--;
		}
		while(globalQueue.isEmpty());
		assertEquals(10, globalQueue.size());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (server != null)
			server.stop();
		if(excecutor != null){
			excecutor.shutdown();
		}
	}

	private static void startJettyServer() {
		excecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Server server = new Server(5559);
					ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
					context.setContextPath("/");
					server.setHandler(context);

					ServletHandler handler = new ServletHandler();
					ServletHolder holder = new ServletHolder(new TestServlet());
					context.addServlet(holder, "/*");

					FilterHolder fh = handler.addFilterWithMapping(AnalyticsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
					Map<String, String> map = new HashMap<String, String>();
					map.put(AnalyticsConstants.ANALYTICS_HOST, "127.0.0.1");
					map.put(AnalyticsConstants.ANALYTICS_PORT, "5556");
					
					fh.setInitParameters(map);
					context.addFilter(fh, "/*", EnumSet.of(DispatcherType.REQUEST));
					server.start();
					server.join();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

	}
	
	private static void startDummyCollector() {
		excecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Server server = new Server(5556);
					ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
					context.setContextPath("/");
					server.setHandler(context);

					ServletHolder holder = new ServletHolder(new TestCollectorServlet());
					context.addServlet(holder, "/*");
				
					server.start();
					server.join();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}
}
