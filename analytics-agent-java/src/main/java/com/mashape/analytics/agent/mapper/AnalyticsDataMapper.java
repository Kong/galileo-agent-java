package com.mashape.analytics.agent.mapper;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import com.mashape.analytics.agent.modal.Content;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.modal.Message;
import com.mashape.analytics.agent.modal.NameValuePair;
import com.mashape.analytics.agent.modal.Request;
import com.mashape.analytics.agent.modal.Response;
import com.mashape.analytics.agent.modal.Timings;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.analytics.agent.wrapper.ResponseInterceptorWrapper;

public class AnalyticsDataMapper {

	Logger logger = Logger.getLogger(AnalyticsDataMapper.class);

	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	private RequestInterceptorWrapper request;
	private ResponseInterceptorWrapper response;
	Message message;

	public AnalyticsDataMapper(ServletRequest request,
			ServletResponse response) {
		this.request = (RequestInterceptorWrapper) request;
		this.response = (ResponseInterceptorWrapper) response;
	}

	public Entry getAnalyticsData(Date requestReceivedTime, long sendTime,
			long waitTime) {
		Entry entry = new Entry();
		entry.setClientIPAddress(request.getRemoteAddr());
		entry.setServerIPAddress(request.getLocalAddr());
		entry.setStartedDateTime(requestReceivedTime.toString());
		entry.setRequest(mapRequest());
		entry.setResponse(mapResponse());
		entry.setTimings(mapTimings(requestReceivedTime, sendTime, waitTime));
		return entry;
	}

	private void setRequestHeaders(Request requestHar) {
		Enumeration<String> headers = request.getHeaderNames();
		List<NameValuePair> headerList = requestHar.getHeaders();
		int size = 0;
		while (headers.hasMoreElements()) {
			String name = headers.nextElement();
			size += name.getBytes().length;
			NameValuePair pair = new NameValuePair();
			pair.setName(name);
			String value = request.getHeader(name);
			size += value.getBytes().length;
			pair.setValue(value);
			headerList.add(pair);
		}
		requestHar.setHeadersSize(size);
	}

	private void setResponseHeaders(Response responseHar) {
		Collection<String> headers = response.getHeaderNames();
		List<NameValuePair> headerList = responseHar.getHeaders();
		int size = 0;
		for (String name : headers) {
			NameValuePair pair = new NameValuePair();
			size += name.getBytes().length;
			pair.setName(name);
			String value = response.getHeader(name);
			size += value.getBytes().length;
			pair.setValue(value);
			headerList.add(pair);
		}
		responseHar.setHeadersSize(size);
	}

	private Request mapRequest() {
		Request requestHar = new Request();
		requestHar.setBodySize(request.getContentLength());
		requestHar.setContent(mapRequestContent());
		requestHar.setMethod(request.getMethod());
		requestHar.setUrl(request.getRequestURL().toString());
		requestHar.setHttpVersion(request.getProtocol());
		setRequestHeaders(requestHar);
		return requestHar;
	}

	private Content mapRequestContent() {
		Content content = new Content();
		content.setEncoding(request.getCharacterEncoding());
		String mimeType = request.getContentType();
		content.setMimeType(request.getContentType());
		content.setMimeType(DEFAULT_MIME_TYPE);
		if (mimeType != null && mimeType.length() > 0) {
			content.setMimeType(request.getContentType());
		}
		content.setSize(request.getPayload().length());
		content.setText(request.getPayload());
		return content;
	}

	private Response mapResponse() {
		Response responseHar = new Response();
		responseHar.setBodySize(response.getBufferSize());
		responseHar.setContent(mapRequestContent());
		responseHar.setContent(mapResponseContent());
		responseHar.setHttpVersion(request.getProtocol());
		responseHar.setStatus(Integer.toString(response.getStatus()));
		responseHar.setStatusText(responseHar.getStatus());
		setResponseHeaders(responseHar);
		return responseHar;
	}

	private Content mapResponseContent() {
		Content content = new Content();
		content.setEncoding(response.getCharacterEncoding());
		String mimeType = response.getContentType();
		content.setMimeType(response.getContentType());
		content.setMimeType(DEFAULT_MIME_TYPE);
		if (mimeType != null && mimeType.length() > 0) {
			content.setMimeType(mimeType);
		}
		try {
			String payload = new String(response.getClone(),
					response.getCharacterEncoding());
			content.setSize(payload.length());
			content.setText(payload);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		return content;
	}

	private Timings mapTimings(Date requestReceivedTime, long sendTime,
			long waitTime) {
		Timings timings = new Timings();
		timings.setReceive(0);
		timings.setSend(sendTime);
		timings.setWait(waitTime);
		return timings;
	}
}
