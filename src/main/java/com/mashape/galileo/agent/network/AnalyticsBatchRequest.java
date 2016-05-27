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

package com.mashape.galileo.agent.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import com.mashape.galileo.agent.config.AnalyticsConfiguration;
import com.mashape.galileo.agent.modal.ALF;

public class AnalyticsBatchRequest implements Runnable {
	private static Logger LOGGER = Logger.getLogger(AnalyticsBatchRequest.class);
	private AnalyticsConfiguration config;
	List<ALF> alfs;
	
	public AnalyticsBatchRequest(AnalyticsConfiguration config, List<ALF> alfs) {
		this.config = config;
		this.alfs = alfs;
	}

	@Override
	public void run() {
		try {
			if( alfs == null || alfs.isEmpty()){
				return;
			}
			batch(alfs, config.getAlfBatchSize());
		} catch (Exception e) {
			LOGGER.error("failed to send data, dropping data", e);
		}
	}

	private void batch(List<ALF> alfs, int batchSize) throws IllegalStateException, IOException, InterruptedException, ExecutionException, TimeoutException {
		if (alfs == null || alfs.size() < 1)
			return;

		int alfsCount = alfs.size();
		for (int i = 0; i < alfsCount; i += batchSize) {
			send(new ArrayList<ALF>(alfs.subList(i, Math.min(alfsCount, i + batchSize))));
		}
	}

	private void send(List<ALF> alfs) throws IllegalStateException, IOException, InterruptedException, ExecutionException, TimeoutException {
		String data = config.getGson().toJson(alfs);
		LOGGER.debug(String.format("sending batch of (%d) ALFs",  alfs.size()));
		HttpPost post = new HttpPost(config.getAnalyticsServerURI());
		post.addHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
		post.setEntity(new StringEntity(data, "UTF-8"));
		HttpRequestFutureTask<Boolean> futureTask = config.getFutureRequestExecutor().execute(post, null, new AnalyticsResponseHandler(), new AnalyticsResponseCallback());
		post.releaseConnection();
	}

}
