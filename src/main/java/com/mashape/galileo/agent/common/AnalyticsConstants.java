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
 * Common constants
 *
 */
public class AnalyticsConstants {
	public static final String ANALYTICS_HOST = "galileo.host";
	public static final String ANALYTICS_PORT = "galileo.port";
	public static final String ANALYTICS_USE_HTTPS = "galileo.use_https";
	public static final String ANALYTICS_CONNECTION_COUNT = "galileo.connection_count";
	public static final String ANALYTICS_WORKER_COUNT = "galileo.workers_count";
	public static final String ANALYTICS_TOKEN = "galileo.service_token";
	public static final String ANALYTICS_ENABLED = "galileo.enabled";
	public static final String SEND_BODY = "galileo.log_bodies";
	public static final String ANALYTICS_ENVIRONMENT = "galileo.environment";
	public static final String ANALYTICS_FLUSH_INTERVAL = "galileo.flush_timeout";
	public static final String ANALYTICS_QUEUE_SIZE = "galileo.queue_size";
	public static final String ANALYTICS_BATCH_SIZE = "galileo.batch_size";
	public static final String ANALYTICS_RETRY_COUNT = "galileo.retry_count";
	public static final String ANALYTICS_REQUEST_ALLOWED_SIZE = "galileo.request_size";
	public static final String ANALYTICS_RESPONSET_ALLOWED_SIZE = "galileo.response_size";
	public static final String ANALYTICS_BATCH_SIZE_MB = "galileo.batch_size_mb";
	public static final String ANALYTICS_BODY_SIZE_MB = "galileo.body_size_mb";
	public static final String CONNECTION_KEEPALIVE_TIME = "galileo.connection_timeout";
	public static final String SHOW_STATUS_TICKER = "galileo.log_status";
	public static final String STATUS_TICKER_INTERVAL = "galileo.log_status_interval";
	public static final String HAR_VERSION = "1.2";

	public static final String AGENT_NAME = "galileo-agent-java";
	public static final String AGENT_VERSION = "1.0.0";
	public static final String CLIENT_IP_ADDRESS = "clientIpAddress";
	public static final String ALF_VERSION = "1.1.0";
	public static final String ALF_VERSION_PREFIX = "alf_1.1.0 ";

	public static final String ENCODING_BASE64 = "base64";

	public static final String DEFAULT_ANALYTICS_HOST = "collector.galileo.mashape.com";
	public static final String DEFAULT_ANALYTICS_SERVER_PORT = "80";
	public static final boolean DEFAULT_USE_HTTPS = false;
	public static final String DEFAULT_ANALYTICS_SERVER_PATH = "/1.1.0/batch";
	public static final int DEFAULT_TASK_QUEUE_SIZE = 5000;
	public static final int DEFAULT_CONNECTION_COUNT = 40;
	public static final int DEFAULT_WORKER_COUNT_MAX = Runtime.getRuntime().availableProcessors();
	public static final int MAX_CONNECTION_COUNT = 1024;
	public static final int DEFAULT_TICKER_INTERVAL_TIME = 300;
	public static final int DEFAULT_ANALYTICS_BATCH_SIZE = 1000;
	public static final int MAX_ANALYTICS_BATCH_SIZE = 1000;
	public static final int DEFAULT_ANALYTICS_QUEUE_SIZE = 2 * DEFAULT_ANALYTICS_BATCH_SIZE;
	public static final int DEFAULT_ANALYTICS_RETRY_COUNT = 0;
	public static final int MAX_ANALYTICS_RETRY_COUNT = 10;
	public static final int DEFAULT_ANALYTICS_FLUSH_INTERVAL = 2;
	public static final int MAX_ANALYTICS_FLUSH_INTERVAL = 60;
	public static final int DEFAULT_CONNECTION_KEEP_ALIVE_TIME = 30;
	public static final int MAX_CONNECTION_KEEP_ALIVE_TIME = 60;
	public static final int DEFAULT_BATCHED_ALF_ALLOWED_SIZE_MB = 500;
	public static final int DEFAULT_SINGLE_ALF_ALLOWED_SIZE_MB = 10;
	public static final int DEFAULT_REQUEST_BODY_ALLOWED_BYTES = 2;
	public static final int DEFAULT_RESPONSE_BODY_ALLOWED_BYTES = 2;
	public static final String DEFAULT_SEND_BODY = "none";
	public static final String SEND_REQUEST_BODY = "request";
	public static final String SEND_RESPONSE_BODY = "response";
	public static final String SEND_ALL_BODY = "all";
}
