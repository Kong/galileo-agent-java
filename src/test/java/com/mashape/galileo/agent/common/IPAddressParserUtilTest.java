package com.mashape.galileo.agent.common;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class IPAddressParserUtilTest {
	
	@Mocked
	private HttpServletRequest request;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testForwadedMultiFor() {
		new NonStrictExpectations() {
			{
				request.getHeader("Forwarded");
				result = "for=192.0.2.43, for=198.51.100.17";
				request.getRemoteAddr();
				result = "192.0.2.41";
			}
		};
		String clientIpAddress = IPAddressParserUtil.getClientIPAddress(request);
		assertEquals("192.0.2.43", clientIpAddress);
		
		new NonStrictExpectations() {
			{
				request.getHeader("Forwarded");
				result = "For=\"[2001:db8:cafe::17]:4711\"";
				request.getRemoteAddr();
				result = "192.0.2.41";
			}
		};
		clientIpAddress = IPAddressParserUtil.getClientIPAddress(request);
		assertEquals("2001:db8:cafe::17", clientIpAddress);
		
		new NonStrictExpectations() {
			{
				request.getHeader("Forwarded");
				result = "for=192.0.2.60;proto=http;by=203.0.113.43";
				request.getRemoteAddr();
				result = "192.0.2.41";
			}
		};
		clientIpAddress = IPAddressParserUtil.getClientIPAddress(request);
		assertEquals("192.0.2.60", clientIpAddress);
	}
	
	@Test
	public void testForwadedSingleFor() {
		new NonStrictExpectations() {
			{
				request.getHeader("Forwarded");
				result = "for=192.0.2.43";
				request.getRemoteAddr();
				result = "192.0.2.41";
			}
		};
		String clientIpAddress = IPAddressParserUtil.getClientIPAddress(request);
		assertEquals("192.0.2.43", clientIpAddress);
		
		new NonStrictExpectations() {
			{
				request.getHeader("Forwarded");
				result = " for=192.0.2.43 ";
				request.getRemoteAddr();
				result = "192.0.2.41";
			}
		};
		clientIpAddress = IPAddressParserUtil.getClientIPAddress(request);
		assertEquals("192.0.2.43", clientIpAddress);
		
		new NonStrictExpectations() {
			{
				request.getHeader("Forwarded");
				result = " for=192.0.2.43:80 ";
				request.getRemoteAddr();
				result = "192.0.2.41";
			}
		};
		clientIpAddress = IPAddressParserUtil.getClientIPAddress(request);
		assertEquals("192.0.2.43", clientIpAddress);
	}
	
	@Test
	public void testForwadedWrongValue() {
		new NonStrictExpectations() {
			{
				request.getHeader("Forwarded");
				result = "for=abc";
				request.getRemoteAddr();
				result = "192.0.2.41";
			}
		};
		String clientIpAddress = IPAddressParserUtil.getClientIPAddress(request);
		assertEquals("192.0.2.41", clientIpAddress);
		
		new NonStrictExpectations() {
			{
				request.getHeader("Forwarded");
				result = " for=192.0.2.43:abc ";
				request.getRemoteAddr();
				result = "192.0.2.43";
			}
		};
		clientIpAddress = IPAddressParserUtil.getClientIPAddress(request);
		assertEquals("192.0.2.43", clientIpAddress);
		
		new NonStrictExpectations() {
			{
				request.getHeader("Forwarded");
				result = "for=192.0.2.43, for=111";
				request.getRemoteAddr();
				result = "192.0.2.41";
			}
		};
		clientIpAddress = IPAddressParserUtil.getClientIPAddress(request);
		assertEquals("192.0.2.43", clientIpAddress);
	}

}
