package com.mashape.analytics.agent.mapper;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.mashape.analytics.agent.modal.Creator;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.modal.Har;
import com.mashape.analytics.agent.modal.Log;
import com.mashape.analytics.agent.modal.Message;
import com.mashape.analytics.agent.modal.Request;

public class AnalyticsDataMapper {
	
	private static final String SERVICE_TOKEN = "serviceToken";
	private static final String HAR_VERSION = "harVersion";
	private static final String AGENT_NAME = "agentName";
	private static final String AGENT_VERSION = "agentVersion";
	private HttpServletRequest request;
	private HttpServletResponse response;
	private FilterConfig config;
	
	public AnalyticsDataMapper(HttpServletRequest request,
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
		
		return null;
	}

	private Request mapRequest() {
		Request requestHar  = new Request();
		requestHar.setBodySize(request.getContentLength());
		//requestHar.setContent();
		return null;
	}

	private Creator setCreator() {
		Creator creator = new Creator();
		creator.setName(config.getInitParameter(AGENT_NAME));
		creator.setVersion(config.getInitParameter(AGENT_VERSION));
		return null;
	}

}
