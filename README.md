# Mashape Galileo Java Agent [![Build Status][travis-image]][travis-url]

> for more information on Mashape Galileo, please visit [getgalileo.io](https://getgalileo.io/)

## About

The agent is a custom servlet filter which intercepts the request and response and sends it to Galileo Collector server asynchronously to generate analytics information. It needs a web container to run, to send data from a app running in Java SE environment please use our stand alone proxy or use one of our data collection APIs.

## Dependencies

- `javax.servlet-api-3.0.1`
- `jeromq-0.3.4`
- `gson-1.2.17`
- `log4j-1.2.17`
- `guava-14.0.1`

### Testing Dependencies

- `jmockit-1.7`
- `junit-4.12`
- `unirest-java-1.4.5`
- `Jetty`

*see [pom.xml](https://github.com/Mashape/galileo-agent-java/blob/master/pom.xml#L48-L117) for dependencies*

## Installation

### With Maven

```xml
<dependency>
  <groupId>com.mashape.galileo.agent</groupId>
  <artifactId>galileo-analytics</artifactId>
  <version>1.0.0</version>
</dependency>
``` 

### Without Maven

- Download the [jar](https://oss.sonatype.org/content/repositories/releases/com/mashape/galileo/agent/galileo-analytics/)
- clone from [Github](https://github.com/Mashape/galileo-agent-java)

## Usage

Filter has been tested on tomcat and should work with Jetty, Jboss and other servers supporting http servlet api. 
To use the filter you would need to add Analytics filter to web descriptor and set few VM arguments in the server.

Add following arguments to the server

| Property                          | Value                                                                             | Default | Min | Max|
| --------------------------------- | --------------------------------------------------------------------------------- | ------- |----|----|
| `galileo.service_token`                 |Galileo Access Token                           | `-`     | `-`    | `-`   |
| `galileo.use_https`                 |Send Analytics data over HTTPS | `false`     | `false`    | `true`   |
| `galileo.connection_count`            | Minimum number of connections to Collector    | `40`    | `1`  | `1024` |
| `galileo.workers_count`            | Maximum number working thread sending data to Collector   | `2 * # of processor` | `1`    | `any positive number` |
| `galileo.connection_timeout`  | Timeout in seconds before aborting the current connection | `30` | `0` | `60` |
| `galileo.batch_size`            | Total ALF count in batch before flushing | `1000`   | `1`     | `any positive number` |
| `galileo.retry_count`            | Number of retries in case of failures | `0`   | `0`     | `10` |  
| `galileo.enabled`          | Set to `true` to enable analytics     | `false`     | `false`    | `true`  |
| `galileo.environment`           | Galileo Environment Slug	        | `""`    | `-`    | `-`   |
| `galileo.log_bodies`            | Capture & send the full bodies of request & response, valid values (all, none, request, response)  | `none` | `none`    | `all`     |
| `galileo.batch_size_mb`           | Total size in MB of ALF batch when data would be flushed | `500`    | `1`    | `depends on plan`   |
| `galileo.body_size_mb`           | Max size in MB allowed per request/response raw body | `10`    | `1`    | `depends on plan`   |
| `galileo.log_status`           | Set to `true` to log connection status   | `false`    | `false`    | `true`   |
| `galileo.log_status_interval`           | Time interval in seconds between connection status log   | `300` | `1` | `any positive number` |


Update `web.xml`:

```xml
<filter>
	<filter-name>galileo-filter</filter-name>
	<filter-class>com.mashape.galileo.agent.filter.AnalyticsFilter</filter-class>
	<!-- optional -->
	<init-param>
		<param-name>galileo.host</param-name>
		<param-value>collector.galileo.mashape.com</param-value>
	</init-param>
	<init-param>
	<!-- optional -->
	<param-name>galileo.port</param-name>
	 	<param-value>80</param-value>
	</init-param>
</filter>
<filter-mapping>
	<filter-name>galileo-filter</filter-name>
	<url-pattern>/*</url-pattern>
</filter-mapping> 
```

## Copyright and license

Copyright Mashape Inc, 2015.

Licensed under [the MIT License](https://github.com/mashape/galileo-agent-java/blob/master/LICENSE)

[travis-url]: https://travis-ci.org/Mashape/galileo-agent-java
[travis-image]: https://travis-ci.org/Mashape/analytics-agent-java.svg?style=flat
