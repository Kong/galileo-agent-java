package com.mashape.analytics.agent.wrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ResponseInterceptorWrapper extends HttpServletResponseWrapper {

	private ServletOutputStream contentStream;
	private PrintWriter writer;
	private OutputStreamCloner cloner;

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
}
