package com.mashape.analytics.agent.mapper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.mashape.analytics.agent.modal.Content;
import com.mashape.analytics.agent.modal.Creator;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.modal.Har;
import com.mashape.analytics.agent.modal.Log;
import com.mashape.analytics.agent.modal.Message;
import com.mashape.analytics.agent.modal.NameValuePair;
import com.mashape.analytics.agent.modal.Request;
import com.mashape.analytics.agent.modal.Response;
import com.mashape.analytics.agent.modal.Timings;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.analytics.agent.wrapper.ResponseInterceptorWrapper;

public class AnalyticsDataMapper {

	private static final String SERVICE_TOKEN = "serviceToken";
	private static final String HAR_VERSION = "harVersion";
	private static final String AGENT_NAME = "agentName";
	private static final String AGENT_VERSION = "agentVersion";
	
	private RequestInterceptorWrapper request;
	private ResponseInterceptorWrapper response;
	private FilterConfig config;
	Message message;

	public AnalyticsDataMapper(ServletRequest request,
			ServletResponse response, FilterConfig config) {
		this.request = (RequestInterceptorWrapper) request;
		this.response = (ResponseInterceptorWrapper) response;
		this.config = config;
	}

	public Message getAnalyticsData(Date requestReceivedTime, long startTime,
			long endTime) {
		message = new Message();
		message.setHar(setHar(requestReceivedTime, startTime, endTime));
		message.setServiceToken(config.getInitParameter(SERVICE_TOKEN));
		return message;
	}

	private Entry getEntryPerRequest(Date requestReceivedTime, long startTime,
			long endTime) {
		Entry entry = new Entry();
		entry.setClientIPAddress(request.getRemoteAddr());
		entry.setServerIPAddress(request.getLocalAddr());
		entry.setStartedDateTime(requestReceivedTime.toString());
		entry.setRequest(mapRequest());
		entry.setResponse(mapResponse());
		entry.setTimings(mapTimings(requestReceivedTime, startTime, endTime));
		return entry;
	}

	private void setRequestHeaders(Request requestHar) {
		// TODO Auto-generated method stub
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
		content.setMimeType(request.getContentType());
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
		content.setMimeType(response.getContentType());

		try {
			String payload = new String(response.getClone(),
					response.getCharacterEncoding());
			content.setSize(payload.length());
			content.setText(payload);
		} catch (UnsupportedEncodingException e) {
			// suppressed
			e.printStackTrace();
		}
		return content;
	}

	private Timings mapTimings(Date requestReceivedTime, long startTime,
			long endTime) {
		Timings timings = new Timings();
		timings.setReceive(0);
		timings.setSend(0);
		timings.setWait((int) (endTime - startTime));
		return timings;
	}

	private Creator setCreator() {
		Creator creator = new Creator();
		creator.setName(config.getInitParameter(AGENT_NAME));
		creator.setVersion(config.getInitParameter(AGENT_VERSION));
		return creator;
	}

	private Har setHar(Date requestReceivedTime, long startTime, long endTime) {
		Har har = new Har();
		har.setLog(setLog(requestReceivedTime, startTime, endTime));
		return har;
	}

	private Log setLog(Date requestReceivedTime, long startTime, long endTime) {
		Log log = new Log();
		log.setVersion(config.getInitParameter(HAR_VERSION));
		log.setCreator(setCreator());
		Entry entry = getEntryPerRequest(requestReceivedTime, startTime,
				endTime);
		log.getEntries().add(entry);
		return log;
	}
}
