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

package com.mashape.analytics.agent.wrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ResponseInterceptorWrapper extends HttpServletResponseWrapper {

	private ServletOutputStream contentStream;
	private PrintWriter writer;
	private OutputStreamCloner cloner;
	private Map<String, String> headerMap = new HashMap<String, String>();

	public ResponseInterceptorWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public void flushBuffer() throws IOException {
		if (writer != null) {
			writer.flush();
		} else if (contentStream != null) {
			cloner.flush();
		}
	}

	public byte[] getClone() {
		if (cloner != null) {
			return cloner.getClone();
		} else {
			return new byte[0];
		}
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (this.writer != null) {
			throw new IllegalStateException(
					"getOutputStream called after getWriter");
		}

		if (this.contentStream == null) {
			this.contentStream = getResponse().getOutputStream();
			this.cloner = new OutputStreamCloner(this.contentStream);
		}
		return this.cloner;
	}

	public PrintWriter getWriter() throws IOException {
		if (this.contentStream != null) {
			throw new IllegalStateException(
					"getWriter called after getOutputStream ");
		}

		if (writer == null) {
			cloner = new OutputStreamCloner(getResponse().getOutputStream());
			writer = new PrintWriter(new OutputStreamWriter(cloner,
					getResponse().getCharacterEncoding()), true);
		}
		return writer;
	}

	@Override
	public Collection<String> getHeaderNames() {
		Collection<String> names = super.getHeaderNames();
		for (String name : headerMap.keySet()) {
			names.add(name);
		}
		return names;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		Collection<String> values = super.getHeaders(name);
		if (headerMap.containsKey(name)) {
			values.add(headerMap.get(name));
		}
		return values;
	}

	public int getSize(){
		return this.cloner.getClone().length;
	}
}
