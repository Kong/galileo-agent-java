package com.mashape.analytics.agent.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zeromq.ZMQ;

import com.google.gson.Gson;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.modal.Message;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class AnalyticsFilterIntegrationTest {

	private static Server server;
	private static String analyticsData;
	private static AtomicBoolean dataRecieved = new AtomicBoolean(false);

	@BeforeClass
	public static void setup() {
		System.setProperty("analytics.token", "YOUR_SERVICE_TOKEN");
		System.setProperty("analytics.enabled.flag", "true");

		try {
			startZMQServer();
			startJettyServer();
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws Exception {
		HttpResponse<String> jsonResponse = Unirest
				.post("http://127.0.0.1:8083/path")
				.header("accept", "application/json")
				.queryString("apiKey", "123").field("parameter", "valuΩΩΩΩe")
				.field("foo", "bar").asString();
		Unirest.shutdown();
		while (!dataRecieved.get()) {
		}
		Message message = new Gson().fromJson(analyticsData, Message.class);
		System.out.println(analyticsData);
		assertNotNull(message);
		assertNotNull(message.getServiceToken());
		assertNotNull(message.getHar());
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
		assertNotNull(entry.getRequest().getContent());
		assertNotNull(entry.getRequest().getBodySize());
		assertNotNull(entry.getRequest().getQueryString());

		assertNotNull(entry.getRequest().getContent().getText());
		assertNotNull(entry.getRequest().getContent().getEncoding());
		assertNotNull(entry.getRequest().getContent().getMimeType());
		assertNotNull(entry.getRequest().getContent().getSize());

		assertNotNull(entry.getResponse().getHttpVersion());
		assertNotNull(entry.getResponse().getHeaders());
		assertNotNull(entry.getResponse().getHeadersSize());
		assertNotNull(entry.getResponse().getContent());
		assertNotNull(entry.getResponse().getBodySize());

		assertNotNull(entry.getResponse().getContent().getText());
		assertNotNull(entry.getResponse().getContent().getEncoding());
		assertNotNull(entry.getResponse().getContent().getMimeType());
		assertNotNull(entry.getResponse().getContent().getSize());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (server != null)
			server.stop();
	}

	private static void startZMQServer() throws InterruptedException {
		ExecutorService excecutor = Executors.newSingleThreadExecutor();
		excecutor.execute(new Runnable() {

			@Override
			public void run() {
				ZMQ.Context context = ZMQ.context(1);
				ZMQ.Socket receiver = context.socket(ZMQ.PULL);
				receiver.bind("tcp://*:5555");

				while (!Thread.currentThread().isInterrupted()) {
					byte[] request = receiver.recv(0);
					analyticsData = new String(request).trim();
					break;
				}
				receiver.close();
				context.term();
				dataRecieved.set(true);
			}
		});

	}

	private static void startJettyServer() {
		ExecutorService excecutor = Executors.newSingleThreadExecutor();
		excecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Server server = new Server(8083);
					ServletContextHandler context = new ServletContextHandler(
							ServletContextHandler.NO_SESSIONS);
					context.setContextPath("/");
					server.setHandler(context);

					ServletHandler handler = new ServletHandler();
					ServletHolder holder = new ServletHolder(new TestServlet());
					context.addServlet(holder, "/*");

					FilterHolder fh = handler.addFilterWithMapping(
							AnalyticsFilter.class, "/*",
							EnumSet.of(DispatcherType.REQUEST));
					Map<String, String> map = new HashMap<String, String>();
					map.put("analytics.server.url", "127.0.0.1");
					map.put("analytics.server.port", "5555");
					fh.setInitParameters(map);
					context.addFilter(fh, "/*",
							EnumSet.of(DispatcherType.REQUEST));
					server.start();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

	}
}
