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
package com.mashape.galileo.agent.mapper;

import static com.mashape.galileo.agent.common.AnalyticsConstants.AGENT_NAME;
import static com.mashape.galileo.agent.common.AnalyticsConstants.AGENT_VERSION;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ALF_VERSION;
import static com.mashape.galileo.agent.common.AnalyticsConstants.ENCODING_BASE64;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.google.common.io.BaseEncoding;
import com.mashape.galileo.agent.common.AnalyticsConstants;
import com.mashape.galileo.agent.common.IPAddressParserUtil;
import com.mashape.galileo.agent.config.AnalyticsConfiguration;
import com.mashape.galileo.agent.modal.ALF;
import com.mashape.galileo.agent.modal.Content;
import com.mashape.galileo.agent.modal.Creator;
import com.mashape.galileo.agent.modal.Entry;
import com.mashape.galileo.agent.modal.Har;
import com.mashape.galileo.agent.modal.Log;
import com.mashape.galileo.agent.modal.NameValuePair;
import com.mashape.galileo.agent.modal.PostData;
import com.mashape.galileo.agent.modal.Request;
import com.mashape.galileo.agent.modal.Response;
import com.mashape.galileo.agent.modal.Timings;
import com.mashape.galileo.agent.wrapper.RequestInterceptorWrapper;
import com.mashape.galileo.agent.wrapper.ResponseInterceptorWrapper;

/**
 * Maps the analytics data to HAR format
 *
 */
public class AnalyticsDataMapper {

	public static final Logger LOGGER = Logger.getLogger(AnalyticsDataMapper.class);

	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	private RequestInterceptorWrapper request;
	private ResponseInterceptorWrapper response;
	private AnalyticsConfiguration config;

	public AnalyticsDataMapper(RequestInterceptorWrapper request, ResponseInterceptorWrapper response, AnalyticsConfiguration config) {
		this.request = (RequestInterceptorWrapper) request;
		this.response = (ResponseInterceptorWrapper) response;
		this.config = config;
	}

	public ALF getAnalyticsData(Date requestReceivedTime, long sendTime, long waitTime) {
		return getALF(requestReceivedTime, sendTime, waitTime);
	}

	private ALF getALF(Date requestReceivedTime, long sendTime, long waitTime) {
		Entry entry = getEntry(requestReceivedTime, sendTime, waitTime);
		ALF message = new ALF();
		message.setHar(setHar(entry));
		message.setServiceToken(config.getAnalyticsToken());
		message.setEnvironment(config.getEnvironment());
		message.setVersion(ALF_VERSION);
		return message;
	}

	public Entry getEntry(Date requestReceivedTime, long sendTime, long waitTime) {
		Entry entry = new Entry();
		entry.setServerIPAddress(request.getLocalAddr());
		entry.setClientIPAddress(IPAddressParserUtil.getClientIPAddress(request));
		entry.setStartedDateTime(dateAsIso(requestReceivedTime));
		entry.setRequest(mapRequest());
		entry.setResponse(mapResponse());
		entry.setTimings(mapTimings(requestReceivedTime, sendTime, waitTime));
		return entry;
	}

	private Har setHar(Entry entry) {
		Har har = new Har();
		har.setLog(setLog(entry));
		return har;
	}

	private Log setLog(Entry entry) {
		Log log = new Log();
		log.setCreator(setCreator());
		log.getEntries().add(entry);
		return log;
	}

	private Creator setCreator() {
		Creator creator = new Creator();
		creator.setName(AGENT_NAME);
		creator.setVersion(AGENT_VERSION);
		return creator;
	}

	private void setRequestHeaders(Request requestHar) {
		Enumeration<String> headers = request.getHeaderNames();
		List<NameValuePair> headerList = requestHar.getHeaders();
		if(headers.hasMoreElements() == false &&  headerList.size() == 0) return;
		int size = 2; // 2 for CRLF
		while (headers.hasMoreElements()) {
			String name = headers.nextElement();
			size += name.getBytes().length;
			Enumeration<String> values = request.getHeaders(name);
			while (values.hasMoreElements()) {
				NameValuePair pair = new NameValuePair();
				pair.setName(name);
				String headerValue = values.nextElement();
				size += headerValue.getBytes().length;
				pair.setValue(headerValue);
				headerList.add(pair);
				size += 6; // 2 for ": " + 2 for ", " if multiple values present
							// and 2 for CRLF for each header
			}
		}
		requestHar.setHeadersSize(size);
	}

	private void setResponseHeaders(Response responseHar) {
		Collection<String> headers = response.getHeaderNames();
		List<NameValuePair> headerList = responseHar.getHeaders();
		if(headers.size() == 0 &&  headerList.size() == 0) return;
		int size = 2; // 2 for CRLF
		for (String name : headers) {
			Collection<String> values = response.getHeaders(name);
			for (String value : values) {
				NameValuePair pair = new NameValuePair();
				size += name.getBytes().length;
				pair.setName(name);
				size += value.getBytes().length;
				pair.setValue(value);
				headerList.add(pair);
				size += 6;// 2 for ": " + 2 for ", " if multiple values present
							// and 2 for CRLF for each header
			}
		}
		responseHar.setHeadersSize(size);
	}

	private Request mapRequest() {
		boolean sendBody = (AnalyticsConstants.SEND_REQUEST_BODY.equals(config.sendBody()) || AnalyticsConstants.SEND_ALL_BODY.equals(config.sendBody())) ? true : false;
		Request requestHar = new Request();
		requestHar.setBodyCaptured(false);
		int payloadSize = request.getPayload().length;
		if (payloadSize > 0) {
			requestHar.setBodySize(payloadSize);
			requestHar.setBodyCaptured(true);
			if (sendBody)
				requestHar.setPostData(mapRequestContent(sendBody));
		}

		requestHar.setMethod(request.getMethod());
		requestHar.setUrl(request.getRequestURL().toString());
		requestHar.setHttpVersion(request.getProtocol());
		setRequestHeaders(requestHar);
		setQueryString(requestHar);
		return requestHar;
	}

	private void setQueryString(Request requestHar) {
		Enumeration<String> params = request.getParameterNames();
		List<NameValuePair> paramList = requestHar.getQueryString();

		while (params.hasMoreElements()) {
			String name = params.nextElement();
			String[] values = request.getParameterValues(name);
			for (String value : values) {
				NameValuePair pair = new NameValuePair();
				pair.setName(name);
				pair.setValue(value);
				paramList.add(pair);
			}
		}
	}

	private PostData mapRequestContent(boolean sendBody) {
		PostData content = new PostData();
		content.setEncoding(ENCODING_BASE64);
		String mimeType = request.getContentType();
		content.setMimeType(DEFAULT_MIME_TYPE);
		if (mimeType != null && mimeType.length() > 0) {
			content.setMimeType(request.getContentType());
		}
		try {
			content.setText(BaseEncoding.base64().encode(request.getPayload()));
		} catch (Exception e) {
			LOGGER.error("failed to encode request body", e);
			content = null;
		}

		return content;
	}

	private Response mapResponse() {
		boolean sendBody = (AnalyticsConstants.SEND_RESPONSE_BODY.equals(config.sendBody()) || AnalyticsConstants.SEND_ALL_BODY.equals(config.sendBody())) ? true : false;
		Response responseHar = new Response();
		responseHar.setBodyCaptured(false);
		int payloadSize = response.getClone().length;
		if (payloadSize > 0) {
			responseHar.setBodySize(payloadSize);
			responseHar.setBodyCaptured(true);
			if (sendBody)
				responseHar.setContent(mapResponseContent(sendBody));
		}
		
		responseHar.setHttpVersion(request.getProtocol());
		responseHar.setStatus(response.getStatus());
		responseHar.setStatusText(Integer.toString(responseHar.getStatus()));
		setResponseHeaders(responseHar);
		return responseHar;
	}

	private Content mapResponseContent(boolean sendBody) {
		Content content = new Content();
		content.setEncoding(ENCODING_BASE64);
		String mimeType = response.getContentType();
		content.setMimeType(DEFAULT_MIME_TYPE);
		if (mimeType != null && mimeType.length() > 0) {
			content.setMimeType(mimeType);
		}
		content.setText(BaseEncoding.base64().encode(response.getClone()));
		return content;
	}

	private Timings mapTimings(Date requestReceivedTime, long sendTime, long waitTime) {
		Timings timings = new Timings();
		timings.setReceive(0);
		timings.setSend(sendTime);
		timings.setWait(waitTime);
		timings.setBlocked(-1);
		timings.setConnect(-1);
		return timings;
	}

	private String dateAsIso(Date date) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.s'Z'");
		df.setTimeZone(tz);
		return df.format(new Date());
	}
}
