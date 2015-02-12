package com.mashape.analytics.agent.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class OutputStreamCloner extends ServletOutputStream {
	private OutputStream outputStream ;
	private ByteArrayOutputStream clonedStream = new ByteArrayOutputStream();
	
	public OutputStreamCloner(OutputStream contentStream){
		this.outputStream = contentStream;
	}
	
	@Override
	public void write(int data) throws IOException {
		this.outputStream.write(data);
		this.clonedStream.write(data);
	}
	
	public byte[] getClone(){
		return this.clonedStream.toByteArray();
	}
}
