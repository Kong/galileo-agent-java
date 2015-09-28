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

package com.mashape.galileo.agent.wrapper;

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
	private OutputStreamCloner clonerStream;
	private HttpServletResponse orignalResponse = null;

	public ResponseInterceptorWrapper(HttpServletResponse response) {
		super(response);
		orignalResponse = response;
	}

	public void finishResponse() {
		try {
			if (writer != null) {
				writer.close();
			} else {
				if (clonerStream != null) {
					clonerStream.close();
				}
			}
		} catch (IOException e) {
		}
	}

	@Override
	public void flushBuffer() throws IOException {
		if (writer != null) {
			writer.flush();
		} else if (contentStream != null) {
			clonerStream.flush();
		}
	}

	public byte[] getClone() {
		if (clonerStream != null) {
			return clonerStream.getClone();
		} else {
			return new byte[0];
		}
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getOutputStream called after getWriter");
		}

		if (contentStream == null) {
			contentStream = orignalResponse.getOutputStream();
			clonerStream = new OutputStreamCloner(contentStream);
		}
		return clonerStream;
	}

	public PrintWriter getWriter() throws IOException {
		if (writer != null) {
			return (writer);
		}

		if (contentStream != null) {
			throw new IllegalStateException("getWriter called after getOutputStream ");
		}

		clonerStream = new OutputStreamCloner(orignalResponse.getOutputStream());
		writer = new PrintWriter(new OutputStreamWriter(clonerStream, orignalResponse.getCharacterEncoding()), true);
		return writer;
	}

	public int getSize() {
		return clonerStream.getClone().length;
	}
}
