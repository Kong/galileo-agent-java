package com.mashape.analytics.agent.mapper;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.mashape.analytics.agent.modal.Content;
import com.mashape.analytics.agent.modal.Creator;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.modal.Har;
import com.mashape.analytics.agent.modal.Log;
import com.mashape.analytics.agent.modal.Message;
import com.mashape.analytics.agent.modal.Request;
import com.mashape.analytics.agent.modal.Response;
import com.mashape.analytics.agent.wrapper.RequestInterceptorWrapper;

public class AnalyticsDataMapper {
	
	private static final String SERVICE_TOKEN = "serviceToken";
	private static final String HAR_VERSION = "harVersion";
	private static final String AGENT_NAME = "agentName";
	private static final String AGENT_VERSION = "agentVersion";
	private RequestInterceptorWrapper request;
	private HttpServletResponse response;
	private FilterConfig config;
	
	public AnalyticsDataMapper(RequestInterceptorWrapper request,
			HttpServletResponse response, FilterConfig config) {
		this.request = request;
		this.response = response;
		this.config = config;
	}
	
	public Message getAnalyticsData(){
		Message message = new Message();
		message.setHar(setHar(request,response));
		message.setServiceToken(config.getInitParameter(SERVICE_TOKEN));
		return message;
	}

	private Har setHar(HttpServletRequest req, HttpServletResponse res) {
		Har har = new Har();
		har.setLog(setLog(req,res));
		return har;
	}

	private Log setLog(HttpServletRequest req, HttpServletResponse res) {
		Log log = new Log();
		log.setVersion(config.getInitParameter(HAR_VERSION));
		log.setCreator(setCreator());
		Entry entry = getEntryPerRequest();
		return log;
	}

	private Entry getEntryPerRequest() {
		Entry entry = new Entry();
		entry.setClientIPAddress(request.getRemoteAddr());
		entry.setServerIPAddress(request.getLocalAddr());
		entry.setRequest(mapRequest());
		entry.setResponse(mapResponse());
		return entry;
	}

	private Response mapResponse() {
		Response responseHar  = new Response();
		responseHar.setBodySize(request.getContentLength());
		responseHar.setContent(mapRequestContent());
		responseHar.setContent(mapResponseContent());
		//requestHar.setHeadersSize();
		return responseHar;
	}

	private Request mapRequest() {
		Request requestHar  = new Request();
		requestHar.setBodySize(request.getContentLength());
		requestHar.setContent(mapRequestContent());
		//requestHar.setHeadersSize();
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
	
	private Content mapResponseContent() {
        Content content = new Content();
        content.setEncoding(response.getCharacterEncoding());
        content.setMimeType(response.getContentType());
        //content.setSize(response.getPayload().length());
        //content.setText(response.getPayload());
		return content;
	}

	private Creator setCreator() {
		Creator creator = new Creator();
		creator.setName(config.getInitParameter(AGENT_NAME));
		creator.setVersion(config.getInitParameter(AGENT_VERSION));
		return creator;
	}

}
