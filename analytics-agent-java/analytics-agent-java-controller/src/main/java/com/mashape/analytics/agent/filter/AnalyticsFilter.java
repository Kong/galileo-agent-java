package com.mashape.analytics.agent.filter;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class AnalyticsFilter implements Filter {
	
	private ExecutorService analyticsServicexeExecutor;
	private int poolSize ;

	@Override
	public void destroy() {
		analyticsServicexeExecutor.shutdown();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain arg2) throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest request = (HttpServletRequest) req;
		request.startAsync();
		analyticsServicexeExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				
				
			}
		});
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		poolSize = Integer.parseInt(config.getInitParameter("poolSize"));
		analyticsServicexeExecutor = Executors.newFixedThreadPool(poolSize);
	}

}
