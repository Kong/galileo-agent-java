package com.mashape.analytics.agent.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ResponseInterceptorWrapper extends HttpServletResponseWrapper {

	private ServletOutputStream contentStream;
	private PrintWriter writer;
	private OutputStreamWrapper clonedStream;

	public ResponseInterceptorWrapper(HttpServletResponse response) {
		super(response);
	}

	public PrintWriter getWriter() throws IOException {
		if (this.contentStream != null) {
			throw new IllegalStateException(
					"getWriter called after getOutputStream ");
		}

		if (writer == null) {
			clonedStream = new OutputStreamWrapper(getResponse()
					.getOutputStream());
			writer = new PrintWriter(new OutputStreamWriter(clonedStream,
					getResponse().getCharacterEncoding()), true);
		}
		return writer;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (this.writer != null) {
			throw new IllegalStateException(
					"getOutputStream called after getWriter");
		}

		if (this.contentStream == null) {
			this.contentStream = getResponse().getOutputStream();
			this.clonedStream = new OutputStreamWrapper(this.clonedStream);
		}
		return this.clonedStream;
	}
	
	@Override
	public void flushBuffer() throws IOException {
		if (writer != null) {
            writer.flush();
        } else if (contentStream != null) {
        	clonedStream.flush();
        }
	}
	
	public byte[] getClone() {
        if (clonedStream != null) {
            return clonedStream.getClone();
        } else {
            return new byte[0];
        }
    }
}
