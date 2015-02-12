package com.mashape.analytics.agent.wrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RequestInterceptorWrapper extends HttpServletRequestWrapper {

	private final String payload;

	public RequestInterceptorWrapper(HttpServletRequest request) {
		super(request);
		StringBuilder content = new StringBuilder();
		BufferedReader reader = null;
		try {
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// Suppressed
				}
			}
		}
		payload = content.toString();
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
	
	public String getPayload(){
		return this.payload;
	}

}
