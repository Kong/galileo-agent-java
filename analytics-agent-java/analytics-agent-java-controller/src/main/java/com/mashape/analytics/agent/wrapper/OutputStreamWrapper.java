package com.mashape.analytics.agent.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class OutputStreamWrapper extends ServletOutputStream {
	private OutputStream contentStream ;
	private ByteArrayOutputStream clonedStream = new ByteArrayOutputStream();
	
	public OutputStreamWrapper(OutputStream contentStream){
		this.contentStream = contentStream;
	}

	@Override
	public void write(int data) throws IOException {
		this.contentStream.write(data);
	}
	
	public byte[] getClone(){
		return this.clonedStream.toByteArray();
	}

}
