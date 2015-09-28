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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class OutputStreamCloner extends ServletOutputStream {
	private ServletOutputStream outputStream;
	private ByteArrayOutputStream clonedStream = null;
	protected boolean closed = false;

	public OutputStreamCloner(ServletOutputStream outputStream) {
		super();
		closed = false;
		this.outputStream = outputStream;
		clonedStream = new ByteArrayOutputStream();
	}

	public void close() throws IOException {
		if (closed) {
			throw new IOException("This output stream has already been closed");
		}
		flush();
		outputStream.close();
		clonedStream.close();
		closed = true;
	}

	public void flush() throws IOException {
		if (closed) {
			throw new IOException("Cannot flush a closed output stream");
		}
		outputStream.flush();
		clonedStream.flush();
	}

	public byte[] getClone() {
		return clonedStream.toByteArray();
	}

	@Override
	public void write(int data) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		this.outputStream.write((byte) data);
		this.clonedStream.write((byte) data);
	}
 
	@Override
	public void write(byte data[]) throws IOException {
		write(data, 0, data.length);
	}
	
	@Override
	public void write(byte data[], int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		this.outputStream.write(data, off, len);
		this.clonedStream.write(data, off, len);
	}

	public boolean closed() {
		return (this.closed);
	}

	public void reset() {
		// noop
	}
}
