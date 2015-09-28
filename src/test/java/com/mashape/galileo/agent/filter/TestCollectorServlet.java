package com.mashape.galileo.agent.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.galileo.agent.modal.ALF;

public class TestCollectorServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		out.print("Filter test");
		out.flush();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String data = "";
		if ("POST".equalsIgnoreCase(req.getMethod())) {
			Scanner s = new Scanner(req.getInputStream(), "UTF-8").useDelimiter("\\A");
			data = s.hasNext() ? s.next() : "";
			Type type = new TypeToken<List<ALF>>(){}.getType();
			List<ALF> messages = new Gson().fromJson(data, type);
			AnalyticsFilterIntegrationTest.globalQueue.addAll(messages);
			s.close();
		}
		resp.setStatus(200);
		resp.getWriter().print("(All ALFs saved: 1/1)");
		resp.flushBuffer();
	}
}
