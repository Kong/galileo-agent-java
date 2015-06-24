# Mashape Analytics Java Agent

> for more information on Mashape Analytics, please visit [apianalytics.com](https://www.apianalytics.com)

## About

The agent is a custom servlet filter which intercepts the request and response and sends it to API Analytics server asynchronously to generate analytics information. It needs a web container to run, to send data from a app running in Java SE environment please use our standalone proxy or use one of our data collection APIs.

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
- Jetty

*see [pom.xml](https://github.com/Mashape/analytics-agent-java/blob/master/pom.xml#L48-L117) for dependencies*

## Installation

### With Maven

```xml
<dependency>
  <groupId>com.mashape.analytics.agent</groupId>
  <artifactId>mashape-analytics</artifactId>
  <version>1.0.0</version>
</dependency>
``` 

### Without Maven

- Download the [jar](https://oss.sonatype.org/content/repositories/releases/com/mashape/analytics/agent/analytics-java-agent/)
- clone from [Github](https://github.com/Mashape/analytics-agent-java)

## Usage

Filter has been tested on tomcat and should work with Jetty, Jboss and other servers supporting http servlet api. 
To use the filter you would need to add Analytics filter to web descriptor and set few VM arguments in the server.

Add following arguments to the server

| Property                          | Value                                                                             | Default |
| --------------------------------- | --------------------------------------------------------------------------------- | ------- |
| `analytics.token`                 | Mashape Analytics Access Token                                                    | `-`     |
| `analytics.socket.min`            | Minimum number of threads/sockets to opened for connection to analytics server    | `10`    |
| `analytics.socket.max`            | Maximum number of threads/sockets allowed to live in pool                         | `20`    |
| `analytics.socket.keepalivetime`  | When the number of threads are greater than the min, this is the maximum time that excess idle threads will wait for new tasks before terminating | `-` |
| `analytics.queue.size`            | Size of the queue for holding the tasks of transferring data to analytics server  | `100`   |
| `analytics.enabled.flag`          | Set to `true` to enable analytics                                                 | `-`     |
| `analytics.environment`           | Server environment name                                                           | `""`    |
  
Update `web.xml`:

```xml
<filter>
  <filter-name>apianalytics-filter</filter-name>
  <filter-class>com.mashape.analytics.agent.filter.AnalyticsFilter</filter-class>
  <init-param>
    <param-name>analytics.server.url</param-name>
    <param-value>socket.analytics.mashape.com</param-value>
  </init-param>
  <init-param>
    <param-name>analytics.server.port</param-name>
    <param-value>5500</param-value>
  </init-param>
</filter>
<filter-mapping>
  <filter-name>apianalytics-filter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping> 
```

## Copyright and license

Copyright Mashape Inc, 2015.

Licensed under [the MIT License](https://github.com/mashape/analytics-agent-java/blob/master/LICENSE)