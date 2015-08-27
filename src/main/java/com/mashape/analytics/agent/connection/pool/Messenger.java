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

package com.mashape.analytics.agent.connection.pool;

import static com.mashape.analytics.agent.common.AnalyticsConstants.AGENT_NAME;
import static com.mashape.analytics.agent.common.AnalyticsConstants.AGENT_VERSION;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ALF_VERSION;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ALF_VERSION_PREFIX;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_DATA;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_SERVER_URL;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ANALYTICS_TOKEN;
import static com.mashape.analytics.agent.common.AnalyticsConstants.CLIENT_IP_ADDRESS;
import static com.mashape.analytics.agent.common.AnalyticsConstants.ENVIRONMENT;
import static com.mashape.analytics.agent.common.AnalyticsConstants.HAR_VERSION;

import java.util.Map;

import org.apache.log4j.Logger;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.google.gson.Gson;
import com.mashape.analytics.agent.modal.Creator;
import com.mashape.analytics.agent.modal.Entry;
import com.mashape.analytics.agent.modal.Har;
import com.mashape.analytics.agent.modal.Log;
import com.mashape.analytics.agent.modal.Message;

/*
 * Opens a connection to Analytics server and sends data
 */
public class Messenger implements Executor {

	private static Logger LOGGER = Logger.getLogger(Messenger.class);

	private final ZContext context;
	private Socket socket;

	public Messenger(ZContext context) {
		this.context = context;
		socket = getSocket();
	}

	private Socket getSocket() {
		if (socket == null) {
			socket = context.createSocket(ZMQ.PUSH);
			socket.setLinger(0);
			socket.connect("tcp://" + AnalyticsConfiguration.getConfig().getAnalyticsServerUrl() + ":"
					+ AnalyticsConfiguration.getConfig().getAnalyticsServerPort());
			LOGGER.debug("Socket created: " + socket);
		}
		return socket;
	}

	public void execute(Map<String, Object> analyticsData) {
		int tryLeft = 3;
		while (tryLeft > 0) {
			try {
				send(analyticsData);
				break;
			} catch (Exception e) {
				if (tryLeft > 0) {
					context.destroySocket(socket);
					socket = getSocket();
					LOGGER.error("Failed to send data, creating a new socket and trying again: " + socket, e);
				} else {
					LOGGER.error("Failed to send data, dropping data", e);
				}
				tryLeft--;
			}
		}
	}

	private void send(Map<String, Object> analyticsData) {
		Message msg = getMessage(analyticsData);
		socket = getSocket();
		String data = ALF_VERSION_PREFIX + new Gson().toJson(msg);
		socket.send(data);
		LOGGER.debug("Message sent by Thread: " + Thread.currentThread().getName() + " using Socket: " + socket);
	}

	public void terminate() {
		if (socket != null) {
			LOGGER.debug("Socket destroyed " + socket);
			context.destroySocket(socket);
		}
	}

	public Message getMessage(Map<String, Object> analyticsData) {
		Entry entry = (Entry) analyticsData.get(ANALYTICS_DATA);
		Message message = new Message();
		message.setHar(setHar(entry));
		message.setServiceToken(AnalyticsConfiguration.getConfig().getAnalyticsToken());
		message.setClientIPAddress(analyticsData.get(CLIENT_IP_ADDRESS).toString());
		message.setEnvironment(AnalyticsConfiguration.getConfig().getEnvironment());
		message.setVersion(ALF_VERSION);
		return message;
	}

	private Har setHar(Entry entry) {
		Har har = new Har();
		har.setLog(setLog(entry));
		return har;
	}

	private Log setLog(Entry entry) {
		Log log = new Log();
		log.setVersion(HAR_VERSION);
		log.setCreator(setCreator());
		log.getEntries().add(entry);
		return log;
	}

	private Creator setCreator() {
		Creator creator = new Creator();
		creator.setName(AGENT_NAME);
		creator.setVersion(AGENT_VERSION);
		return creator;
	}

	@Override
	protected void finalize() throws Throwable {
		this.terminate();
		LOGGER.debug("Messanger resources destroyed:" + this.toString());
		super.finalize();
	}
}
