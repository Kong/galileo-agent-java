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
package com.mashape.analytics.agent.connection.pool;

import java.util.Map;

import org.apache.log4j.Logger;

import com.mashape.analytics.agent.filter.AnalyticsFilter;

/*
 * Task use a pooled Messenger to send data
 */
public class SendAnalyticsTask implements Runnable {

	final static Logger LOGGER = Logger.getLogger(AnalyticsFilter.class);

	private Map<String, Object> analyticsData;

	public SendAnalyticsTask(Map<String, Object> analyticsData) {
		this.analyticsData = analyticsData;
		LOGGER.debug("New task created:" + this.toString());
	}

	public void run() {
		try{
			MessangerPool.get().execute(analyticsData);
		}
		catch(Exception e){
			LOGGER.error("Failed to send data", e);
		}
		
	}
}
