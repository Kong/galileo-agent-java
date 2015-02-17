package com.mashape.analytics.agent.wrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RequestInterceptorWrapper extends HttpServletRequestWrapper {

	private final String payload;
	private Map<String, String> headerMap = new HashMap<String, String>();

	public RequestInterceptorWrapper(HttpServletRequest request) throws IOException {
		super(request);
		StringBuilder content = new StringBuilder();
		BufferedReader reader = null;

		InputStream in = request.getInputStream();
		if (in != null) {
			reader = new BufferedReader(new InputStreamReader(in));
			char[] buffer = new char[128];
			int bytesCount = -1;
			while ((bytesCount = reader.read(buffer)) > 0) {
				content.append(buffer, 0, bytesCount);
			}
		} else {
			content.append("");
		}

		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				// Suppressed
			}
		}
		payload = content.toString();
	}

	public void addHeader(String name, String value) {
		headerMap.put(name, value);
	}

	@Override
	public String getHeader(String name) {
		String headerValue = super.getHeader(name);
		if (headerMap.containsKey(name)) {
			headerValue = headerMap.get(name);
		}
		return headerValue;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		List<String> names = Collections.list(super.getHeaderNames());
		for (String name : headerMap.keySet()) {
			names.add(name);
		}
		return Collections.enumeration(names);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		List<String> values = Collections.list(super.getHeaders(name));
		if (headerMap.containsKey(name)) {
			values.add(headerMap.get(name));
		}
		return Collections.enumeration(values);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				payload.getBytes());
		ServletInputStream inputStream = new ServletInputStream() {
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}
		};
		return inputStream;
	}

	public String getPayload() {
		return this.payload;
	}

}
