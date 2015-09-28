package com.mashape.galileo.agent.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;
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
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.io.BaseEncoding;
import com.mashape.galileo.agent.config.AnalyticsConfiguration;
import com.mashape.galileo.agent.modal.ALF;
import com.mashape.galileo.agent.modal.Entry;
import com.mashape.galileo.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.galileo.agent.wrapper.ResponseInterceptorWrapper;

@RunWith(JMockit.class)
public class AnalyticsDataMapperTest {
	
	@Tested
	private AnalyticsDataMapper mapper;
	
	@Mocked
	@Injectable
	private AnalyticsConfiguration config;
	
	@Mocked
	@Injectable
	private RequestInterceptorWrapper request;
	
	@Mocked
	@Injectable
	private ResponseInterceptorWrapper response;
	
	private Map<String, List<String>> headerMap = new HashMap<String, List<String>>();

	@Before
	public void setUp() throws Exception {
		List<String> values = new ArrayList<String>();
		values.add("V1");
		headerMap.put("H1", values);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAnalyticsDataMapper() throws UnsupportedEncodingException {
		new NonStrictExpectations() {
			{
				config.getEnvironment();
				result = "TEST";
				config.getAnalyticsToken();
				result = "abcdef";
				config.sendBody();
				result = "all";
				request.getRemoteAddr();
				result = "remotehost";
				request.getLocalAddr();
				result = "localhost";
				request.getHeaderNames();
				result = getHeaderNames();
				request.getHeaders("H1");
				result = Collections.enumeration(headerMap.get("H1"));
				response.getHeaderNames();
				result = getHeaderResponseNames();
				response.getHeaders("H1");
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
				result = "it is payload".getBytes();
				response.getBufferSize();
				result = 5;
				response.getStatus();
				result = 200;
				response.getCharacterEncoding();
				result = "UTF-8";
				response.getSize();
				result = 8;
				response.getClone();
				result = "response".getBytes();
				response.getContentType();
				result = "application/json";
				
			}
		};
		ALF alf = mapper.getAnalyticsData(new Date(), 50, 50);
		Entry entry = alf.getHar().getLog().getEntries().get(0);
		assertNotNull(entry);
		assertNotNull(entry.getStartedDateTime());
		assertNotNull(entry.getRequest());
		assertNotNull(entry.getResponse());
		assertNotNull(entry.getTimings());
		assertEquals("localhost", entry.getServerIPAddress());
		
		assertNotNull(entry.getRequest().getMethod());
		assertNotNull(entry.getRequest().getUrl());
		assertNotNull(entry.getRequest().getHttpVersion());
		assertNotNull(entry.getRequest().getHeaders());
		assertNotNull(entry.getRequest().getHeadersSize());
		assertNotNull(entry.getRequest().getPostData());
		assertNotNull(entry.getRequest().getBodySize());
		
		
		assertEquals("it is payload", new String(BaseEncoding.base64().decode(entry.getRequest().getPostData().getText()), "UTF-8"));
		assertEquals("base64", entry.getRequest().getPostData().getEncoding());
		assertEquals(13, entry.getRequest().getBodySize());
		assertEquals("http://remotehost/todo", entry.getRequest().getUrl());
		assertEquals(1, entry.getRequest().getHeaders().size());
		assertEquals("H1", entry.getRequest().getHeaders().get(0).getName());
		assertEquals("V1", entry.getRequest().getHeaders().get(0).getValue());
		assertEquals("http://remotehost/todo", entry.getRequest().getUrl());
		assertEquals("POST", entry.getRequest().getMethod());
		assertEquals(12, entry.getRequest().getHeadersSize());
		assertEquals("HTTP 1.0", entry.getRequest().getHttpVersion());
		
		assertEquals("response", new String(BaseEncoding.base64().decode(entry.getResponse().getContent().getText()),  "UTF-8"));
		
		assertEquals(8, entry.getResponse().getBodySize());
		assertEquals(1, entry.getRequest().getHeaders().size());
		assertEquals("H1", entry.getResponse().getHeaders().get(0).getName());
		assertEquals("V1", entry.getResponse().getHeaders().get(0).getValue());
		assertEquals(12, entry.getResponse().getHeadersSize());
		assertEquals("HTTP 1.0", entry.getResponse().getHttpVersion());
		assertEquals("application/json", entry.getResponse().getContent().getMimeType());
		assertEquals("base64", entry.getResponse().getContent().getEncoding());
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
	public void testAnalyticsDataMapperNodBody() throws UnsupportedEncodingException {
		new NonStrictExpectations() {
			{	
				config.getEnvironment();
				result = "TEST";
				config.getAnalyticsToken();
				result = "abcdef";
				config.sendBody();
				result = "none";
				request.getRemoteAddr();
				result = "remotehost";
				request.getLocalAddr();
				result = "localhost";
				request.getHeaderNames();
				result = getHeaderNames();
				request.getHeaders("H1");
				result = Collections.enumeration(headerMap.get("H1"));
				response.getHeaderNames();
				result = getHeaderResponseNames();
				response.getHeaders("H1");
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
				result = "it is payload".getBytes();
				response.getBufferSize();
				result = 5;
				response.getStatus();
				result = 200;
				response.getCharacterEncoding();
				result = "UTF-8";
				response.getSize();
				result = 8;
				response.getClone();
				result = "response".getBytes();
				response.getContentType();
				result = "application/json";
				
			}
		};
		
		ALF alf = mapper.getAnalyticsData(new Date(), 50, 50);
		Entry entry = alf.getHar().getLog().getEntries().get(0);
		assertNotNull(entry);
		assertNotNull(entry.getStartedDateTime());
		assertNotNull(entry.getRequest());
		assertNotNull(entry.getResponse());
		assertNotNull(entry.getTimings());
		assertEquals("localhost", entry.getServerIPAddress());
		
		assertNotNull(entry.getRequest().getMethod());
		assertNotNull(entry.getRequest().getUrl());
		assertNotNull(entry.getRequest().getHttpVersion());
		assertNotNull(entry.getRequest().getHeaders());
		assertNotNull(entry.getRequest().getHeadersSize());
		assertNull(entry.getRequest().getPostData());
		assertNotNull(entry.getRequest().getBodySize());
		
	
		assertEquals(13, entry.getRequest().getBodySize());
		assertEquals("http://remotehost/todo", entry.getRequest().getUrl());
		assertEquals(1, entry.getRequest().getHeaders().size());
		assertEquals("H1", entry.getRequest().getHeaders().get(0).getName());
		assertEquals("V1", entry.getRequest().getHeaders().get(0).getValue());
		assertEquals("http://remotehost/todo", entry.getRequest().getUrl());
		assertEquals("POST", entry.getRequest().getMethod());
		assertEquals(12, entry.getRequest().getHeadersSize());
		assertEquals("HTTP 1.0", entry.getRequest().getHttpVersion());
		
		assertEquals(8, entry.getResponse().getBodySize());
		assertEquals(1, entry.getRequest().getHeaders().size());
		assertEquals("H1", entry.getResponse().getHeaders().get(0).getName());
		assertEquals("V1", entry.getResponse().getHeaders().get(0).getValue());
		assertEquals(12, entry.getResponse().getHeadersSize());
		assertEquals("HTTP 1.0", entry.getResponse().getHttpVersion());
	}

}
