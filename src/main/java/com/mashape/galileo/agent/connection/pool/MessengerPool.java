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

package com.mashape.galileo.agent.connection.pool;

import org.apache.log4j.Logger;
import org.zeromq.ZContext;

import com.mashape.galileo.agent.filter.AnalyticsFilter;

/*
 * Messenger pool with each messenger having their own zmq socket 
 */
public class MessengerPool {

	final static Logger logger = Logger.getLogger(MessengerPool.class);
	private static final ZContext context = new ZContext();

	private static final ThreadLocal<Messenger> MESSANGERPOOL = new ThreadLocal<Messenger>() {

		@Override
		public void remove() {
			Messenger messenger = MESSANGERPOOL.get();
			logger.debug("Messenger removed: " + messenger.toString() + " for thread: " + Thread.currentThread().getName());
			messenger.terminate();
			super.remove();
		}

		@Override
		protected Messenger initialValue() {
			Messenger messenger = new Messenger(context);
			logger.debug("Messenger Created: " + messenger.toString() + " for thread: " + Thread.currentThread().getName());
			return messenger;
		}
	};

	public static Messenger get() {
		return MESSANGERPOOL.get();
	}

	public static void remove() {
		MESSANGERPOOL.remove();
	}

	public static void terminate() {
		context.destroy();
	}
}
