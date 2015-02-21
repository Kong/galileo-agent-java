package com.mashape.analytics.agent.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.analytics.agent.wrapper.ResponseInterceptorWrapper;

@RunWith(JMockit.class)
public class AnalyticsDataMapperTest {
	
	@Tested
	private AnalyticsDataMapper mapper;
	
	@Mocked
	@Injectable
	private RequestInterceptorWrapper request;
	
	@Mocked
	@Injectable
	private ResponseInterceptorWrapper response;
	
	private Map<String, String> headerMap = new HashMap<String, String>();

	@Before
	public void setUp() throws Exception {
		headerMap.put("H1", "V1");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAnalyticsDataMapper() {
		new NonStrictExpectations() {
			{
				request.getRemoteAddr();
				result = "remotehost";
				request.getLocalAddr();
				result = "localhost";
				request.getHeaderNames();
				result = getHeaderNames();
				request.getHeader("H1");
				result = headerMap.get("H1");
				response.getHeaderNames();
				result = getHeaderResponseNames();
				response.getHeader("H1");
				result = headerMap.get("H1");
				request.getContentLength();
				result = "it is payload".getBytes().length;
				request.getMethod();
				result = "POST";
				request.getRequestURL();
				result = new StringBuffer("http://remotehost/todo");
				request.getProtocol();
				result = "HTTP 1.0";
				request.getCharacterEncoding();
				result = "UTF-8";
				request.getContentType();
				result = "application/json";
				request.getPayload();
				result = "it is payload";
				response.getBufferSize();
				result = 5;
				response.getStatus();
				result = 200;
				response.getCharacterEncoding();
				result = "UTF-8";
				response.getClone();
				result = "response".getBytes();
				response.getContentType();
				result = "application/json";
				
			}
		};
		
		Entry entry = mapper.getAnalyticsData(new Date(), 50, 50);
		assertNotNull(entry);
		assertNotNull(entry.getStartedDateTime());
		assertNotNull(entry.getRequest());
		assertNotNull(entry.getResponse());
		assertNotNull(entry.getTimings());
		assertEquals("remotehost", entry.getClientIPAddress());
		assertEquals("localhost", entry.getServerIPAddress());
		
		assertNotNull(entry.getRequest().getMethod());
		assertNotNull(entry.getRequest().getUrl());
		assertNotNull(entry.getRequest().getHttpVersion());
		assertNotNull(entry.getRequest().getHeaders());
		assertNotNull(entry.getRequest().getHeadersSize());
		assertNotNull(entry.getRequest().getContent());
		assertNotNull(entry.getRequest().getBodySize());
		
		
		assertEquals("it is payload", entry.getRequest().getContent().getText());
		assertEquals(13, entry.getRequest().getBodySize());
		assertEquals("http://remotehost/todo", entry.getRequest().getUrl());
		assertEquals(1, entry.getRequest().getHeaders().size());
		assertEquals("H1", entry.getRequest().getHeaders().get(0).getName());
		assertEquals("V1", entry.getRequest().getHeaders().get(0).getValue());
		assertEquals("http://remotehost/todo", entry.getRequest().getUrl());
		assertEquals("POST", entry.getRequest().getMethod());
		assertEquals(6, entry.getRequest().getHeadersSize());
		assertEquals("HTTP 1.0", entry.getRequest().getHttpVersion());
		
		assertEquals("response", entry.getResponse().getContent().getText());
		assertEquals(8, entry.getResponse().getBodySize());
		assertEquals(1, entry.getRequest().getHeaders().size());
		assertEquals("H1", entry.getResponse().getHeaders().get(0).getName());
		assertEquals("V1", entry.getResponse().getHeaders().get(0).getValue());
		assertEquals(8, entry.getResponse().getHeadersSize());
		assertEquals("HTTP 1.0", entry.getResponse().getHttpVersion());
		assertEquals(8, entry.getResponse().getContent().getSize());
		assertEquals("application/json", entry.getResponse().getContent().getMimeType());
		
	}
	
	public Enumeration<String> getHeaderNames() {
		List<String> names = new ArrayList<String>();
		for (String name : headerMap.keySet()) {
			names.add(name);
		}
		return Collections.enumeration(names);
	}
	
	public Collection<String> getHeaderResponseNames() {
		List<String> names = new ArrayList<String>();
		for (String name : headerMap.keySet()) {
			names.add(name);
		}
		return names;
	}

	@Test
	@Ignore
	public void testGetAnalyticsData() {
		fail("Not yet implemented");
	}

}
