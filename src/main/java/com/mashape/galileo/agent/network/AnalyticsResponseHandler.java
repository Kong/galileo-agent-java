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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class AnalyticsResponseHandler implements ResponseHandler<Boolean> {
	private static Logger LOGGER = Logger.getLogger(AnalyticsResponseHandler.class);

	@Override
	public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			throw new ClientProtocolException("failed to save the batch");
		}

		StatusLine statusLine = response.getStatusLine();
		String responseBody = EntityUtils.toString(entity);
		if (statusLine.getStatusCode() == 200) {
			LOGGER.debug(String.format("successfully saved the batch. (%s)", responseBody));
			return true;
		}
		if (statusLine.getStatusCode() == 207) {
			LOGGER.warn(String.format("collector could not save all ALFs from the batch. (%s)", responseBody));
			return true;
		}
		if (statusLine.getStatusCode() == 400) {
			LOGGER.error(String.format("collector refused to save the batch, dropping batch. Status: (%d) Reason: (%s) Error: (%s)", statusLine.getStatusCode(), statusLine.getReasonPhrase(), responseBody));
			return true;
		}
		if (statusLine.getStatusCode() == 413) {
			LOGGER.error(String.format("collector refused to save the batch, dropping batch. Status: (%d)  Error: (%s)", statusLine.getStatusCode(), responseBody));
			return true;
		}
		LOGGER.error(String.format("failed to save the batch. Status: (%d)  Error: (%s)", statusLine.getStatusCode(), responseBody));
		return false;
	}
}
