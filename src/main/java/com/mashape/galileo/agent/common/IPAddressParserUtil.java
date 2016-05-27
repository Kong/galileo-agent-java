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

package com.mashape.galileo.agent.common;

import javax.servlet.http.HttpServletRequest;
import org.apache.http.conn.util.InetAddressUtils;

/**
 * Utility Class to retrieve Client IP address from request
 * 
 *
 */
public class IPAddressParserUtil {

	private static String[] headers = { 
		"Forwarded", 
		"X-Real-IP", 
		"X-FORWARDED-FOR", 
		"Fastly-Client-IP", 
		"CF-Connecting-IP", 
		"X-Cluster-Client-IP",
		"X-Cluster-Client-IP", 
		"Z-Forwarded-For", 
		"WL-Proxy-Client-IP", 
		"Proxy-Client-IP" 
	};
	
	/**
	 * Returns the true IP address of the client from specified request after parsing the header's value in the following order
	 *  Forwarded
	 *  X-Real-IP 
	 *  X-FORWARDED-FOR
	 *  Fastly-Client-IP
	 *  CF-Connecting-IP
	 *  X-Cluster-Client-IP
	 *  X-Cluster-Client-IP
	 *  Z-Forwarded-For
	 *  WL-Proxy-Client-IP
	 *  Proxy-Client-IP
	 *  
	 *  if none of above headers set fall back to remote address
	 *  
	 * @param request
	 * @return a <code>String</code> containing the the client IP address
	 */
	public static String getClientIPAddress(HttpServletRequest request) {
		for (String header : headers) {
			String ipAddress = request.getHeader(header);
			if (ipAddress == null)
				break;
			if ("Forwarded".equals(header))
				ipAddress = findForwadedClientIpAddress(ipAddress);
			else if ("X-FORWARDED-FOR".equals(header))
				ipAddress = findXFFClientIpAddress(ipAddress);
			if (isValidIp(ipAddress)) {
				return ipAddress;
			}
		}
		return request.getRemoteAddr();
	}
	
	private static String findXFFClientIpAddress(String s) {
		String[] addresses = s.trim().split(",");

		if (addresses.length > 0) {
			return addresses[0].trim();
		}
		return null;
	}

	private static boolean isValidIp(final String ip) {
		String ipAdress = ip.toLowerCase().trim();
		return InetAddressUtils.isIPv4Address(ipAdress) || InetAddressUtils.isIPv6Address(ipAdress);
	}

	private static String findForwadedClientIpAddress(String headerValue) {
		String[] forwardedPairs = headerValue.toLowerCase().trim().split(";");
		for (String pair : forwardedPairs) {
			if (pair.trim().startsWith("for=")) {
				String forwardedPair = pair.trim();
				String forValue;
				if (forwardedPair.contains(",")) {
					String[] forpairs = forwardedPair.split(",");
					String forPair = forpairs[0];
					if (forPair.contains("for=")) {
						forValue = forPair.substring(forPair.indexOf("for=") + 4, forPair.length()).trim();
					} else {
						return null;
					}
				} else {
					forValue = forwardedPair.substring(forwardedPair.indexOf("for=") + 4, forwardedPair.length()).trim();

				}
				if (forValue.matches("\"\\[.*\\]:\\d*\"")) {
					forValue = forValue.substring(2, forValue.indexOf("]"));
				} else if (forValue.matches(".*:\\d*")) {
					forValue = forValue.split(":")[0];
				}
				return forValue;
			}
		}
		return null;
	}
}
