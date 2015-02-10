package com.mashape.analytics.agent.mapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.mashape.analytics.agent.modal.Creator;
import com.mashape.analytics.agent.modal.Har;
import com.mashape.analytics.agent.modal.Log;
import com.mashape.analytics.agent.modal.Message;

public class AnalyticsDataMapper {
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	public JsonObject getAnalyticsData(HttpServletRequest req, HttpServletResponse res, String key){
		JsonObject data = new JsonObject();
		Message message = new Message();
		message.setServiceToken(key);
		message.setHar(setHar(req,res));
		
		return data;
	}

	private Har setHar(HttpServletRequest req, HttpServletResponse res) {
		Har har = new Har();
		har.setLog(setLog(req,res));
		return har;
	}

	private Log setLog(HttpServletRequest req, HttpServletResponse res) {
		Log log = new Log();
		log.setCreator(setCreator(req,res));
		return log;
	}

	private Creator setCreator(HttpServletRequest req, HttpServletResponse res) {
		// TODO Auto-generated method stub
		return null;
	}

}
