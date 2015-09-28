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

/**
 * Static helpers for dealing with property values
 * 
 */
public class PropertyUtil {

	private PropertyUtil() {
		// closed
	}

	/**
	 * Checks the given string is not blank
	 * 
	 * @param val
	 * @return a <code>boolean</code> value, true if given string not null and
	 *         blank, false otherwise
	 */
	public static boolean notBlank(String val) {
		if (val != null && val.length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the int value of given string if it is a number and less than
	 * allowed value, otherwise default value
	 * 
	 * @param stringVal
	 * @param defaultVals
	 * @return a <code>int</code> value, either the given value if its valid or
	 *         the default value
	 */
	public static int getEnvVarOrDefault(String stringVal, int... defaultVals) {
		int allowedMin, allowedMax, defaultVal;
		if (defaultVals.length > 2) {
			defaultVal = defaultVals[0];
			allowedMin = defaultVals[1];
			allowedMax = defaultVals[2];
		} else if (defaultVals.length > 1) {
			defaultVal = defaultVals[0];
			allowedMin = 0;
			allowedMax = defaultVals[1];
		} else {
			defaultVal = defaultVals[0];
			allowedMin = 0;
			allowedMax = Integer.MAX_VALUE;
		}
		if (PropertyUtil.notBlank(stringVal)) {
			try {
				int value = Integer.parseInt(stringVal);
				if (allowedMin <= value && value <= allowedMax) {
					return value;
				}
			} catch (Exception e) {
				return defaultVal;
			}
		}
		return defaultVal;
	}
}
