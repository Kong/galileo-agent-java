package com.mashape.analytics.agent.filter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mashape.analytics.agent.connection.client.ConnectionManager;

public class AnalyticsFilter implements Filter {
	
	private ExecutorService analyticsServicexeExecutor;
	private int poolSize ;
	private String analyticsServerUrl;
	private String analyticsServerPort;
	private String analyticsKey;
	
	
	@Override
	public void destroy() {
		analyticsServicexeExecutor.shutdown();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		request.startAsync();
		chain.doFilter(request, response);
		System.out.println(Thread.currentThread().getName());
		analyticsServicexeExecutor.execute(new Runnable() {
			
			@Override
			public void run() {				
				System.out.println(Thread.currentThread().getName());
				for(int i = 0; i <=1000; i++){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Play:" + i);
				}
				
			}
		});
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		poolSize = Integer.parseInt(config.getInitParameter("poolSize"));
		analyticsServicexeExecutor = Executors.newFixedThreadPool(poolSize);
		analyticsServerUrl = config.getInitParameter("analyticsServerUrl");
		analyticsServerPort = config.getInitParameter("analyticsServerPort");
		analyticsKey = config.getInitParameter("analyticsKey");
	}

}
