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

package com.mashape.apianalytics.agent.connection.pool;

import static com.mashape.apianalytics.agent.common.ApianalyticsConstants.AGENT_NAME;
import static com.mashape.apianalytics.agent.common.ApianalyticsConstants.AGENT_VERSION;
import static com.mashape.apianalytics.agent.common.ApianalyticsConstants.ANALYTICS_DATA;
import static com.mashape.apianalytics.agent.common.ApianalyticsConstants.ANALYTICS_SERVER_PORT;
import static com.mashape.apianalytics.agent.common.ApianalyticsConstants.ANALYTICS_SERVER_URL;
import static com.mashape.apianalytics.agent.common.ApianalyticsConstants.ANALYTICS_TOKEN;
import static com.mashape.apianalytics.agent.common.ApianalyticsConstants.HAR_VERSION;

import java.util.Map;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

import com.google.gson.Gson;
import com.mashape.apianalytics.agent.modal.Creator;
import com.mashape.apianalytics.agent.modal.Entry;
import com.mashape.apianalytics.agent.modal.Har;
import com.mashape.apianalytics.agent.modal.Log;
import com.mashape.apianalytics.agent.modal.Message;
/*
 * Opens a connection to Analytics server and sends data
 */
public class Messenger implements Work {

	Logger logger = Logger.getLogger(Messenger.class);

	private ZMQ.Context context;
	private ZMQ.Socket socket;
	
	public  Messenger(){
		 context = ZMQ.context(1);
		 socket = context.socket(ZMQ.PUSH);
	}

	public void execute(Map<String, Object> analyticsData) {
		Entry entry = (Entry) analyticsData.get(ANALYTICS_DATA);
		String token = analyticsData.get(ANALYTICS_TOKEN).toString();
		Message msg = getMessage(entry, token);
		String data = new Gson().toJson(msg);
		String analyticsServerUrl = analyticsData.get(ANALYTICS_SERVER_URL).toString();
		String port = analyticsData.get(ANALYTICS_SERVER_PORT).toString();
		socket.connect("tcp://" + analyticsServerUrl + ":" + port);
		socket.send(data);
		logger.debug("Message sent:" + data);
	}

	public void terminate() {
		if (socket != null) {
			logger.debug("Closing socket:" + socket.toString());
			socket.close();
		}
		if (context != null) {
			context.term();
		}
	}

	public Message getMessage(Entry entry, String token) {
		Message message = new Message();
		message.setHar(setHar(entry));
		message.setServiceToken(token);
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
}
