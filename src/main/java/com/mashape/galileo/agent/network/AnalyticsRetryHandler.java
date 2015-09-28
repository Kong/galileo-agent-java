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
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.log4j.Logger;

public class AnalyticsRetryHandler implements HttpRequestRetryHandler {
	
	private static Logger LOGGER = Logger.getLogger(AnalyticsRetryHandler.class);
	private final Set<Class<? extends IOException>> nonRetriableClasses;
	private final int retryCount;

	protected AnalyticsRetryHandler(final int retryCount, final Collection<Class<? extends IOException>> clazzes) {
		super();
		this.retryCount = retryCount;
		this.nonRetriableClasses = new HashSet<Class<? extends IOException>>();
		for (final Class<? extends IOException> clazz : clazzes) {
			this.nonRetriableClasses.add(clazz);
		}
	}

	@SuppressWarnings("unchecked")
	public AnalyticsRetryHandler(final int retryCount) {
		this(retryCount, Arrays.asList(InterruptedIOException.class, UnknownHostException.class, ConnectException.class, SSLException.class));
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		
		Args.notNull(exception, "Exception parameter");
		Args.notNull(context, "HTTP context");

		if (executionCount > this.retryCount) {
			return false;
		}

		if (this.nonRetriableClasses.contains(exception.getClass())) {
			return false;
		} else {
			for (final Class<? extends IOException> rejectException : this.nonRetriableClasses) {
				if (rejectException.isInstance(exception)) {
					return false;
				}
			}
		}
		
		if (exception instanceof NoHttpResponseException | exception instanceof SocketException | exception instanceof NoHttpResponseException) {
			LOGGER.warn(String.format("flush rquest failed, Agent will try to resend the request. reson: (%s)", exception.getMessage()));
            return true;
        }

		final HttpClientContext clientContext = HttpClientContext.adapt(context);
		
		if (!clientContext.isRequestSent()) {
			LOGGER.debug("Agent will try to resend the request.");
			return true;
		}

		return false;
	}

}
