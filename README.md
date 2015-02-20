# Analytics Java Agent


Analytics Java Agent is custom servlet filter which intercepts the request and response to generate analytics data which it eventually sends to API Analytics server.


# Installation 

	
# MAVEN


```xml

	<dependency>
		<groupId>com.mashape.analytics.agent</groupId>
		<artifactId>analytics-agent-java</artifactId>
		<version>1.0</version>
	</dependency>

``` 

# Dependencies

```xml
	
	<dependency>
			<groupId>javax.servlet</groupId>
			<artifactId>javax.servlet-api</artifactId>
			<version>3.0.1</version>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>org.zeromq</groupId>
			<artifactId>jeromq</artifactId>
			<version>0.3.4</version>
		</dependency>
		<dependency>
			<groupId>com.google.code.gson</groupId>
			<artifactId>gson</artifactId>
			<version>2.3.1</version>
		</dependency>
		<dependency>
			<groupId>log4j</groupId>
			<artifactId>log4j</artifactId>
			<version>1.2.17</version>
		</dependency>
		<dependency>
			<groupId>com.googlecode.jmockit</groupId>
			<artifactId>jmockit</artifactId>
			<version>1.7</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.8.1</version>
		</dependency>

```


# Configuration for Web Server

Filter has been tested on tomcat and should work with Jetty, Jboss and other servers supporting http servlet api. 
To use the filter you would need to add Analytics filter to web descriptor.  

```xml

	<filter>
		<filter-name>analyticsFilter</filter-name>
		<filter-class>com.mashape.analytics.agent.filter.AnalyticsFilter</filter-class>
		<init-param>
			<param-name>analyticsServerUrl</param-name>
			<param-value>socket.apianalytics.com</param-value>
		</init-param>
		<init-param>
			<param-name>analyticsServerPort</param-name>
			<param-value>5000</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>analyticsFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
```
	
     Add following environment variable to the server
     NAME				= Value
     ANALYTICS_TOKEN = You Api analytics token from [](http://www.apianalyitics.com "Api Analytics") 
     SOCKET_POOL_SIZE_MIN = Minimun number of sockets to opened for connection to analytics server, default is 10
     SOCKET_POOL_SIZE_MAX = Maximum number of sockets pool should keep, default is 20
     THREAD_POOL_SIZE = Number of thread in the pool handling data transfer to analytics server, default is 2* # of processor 
	



	


 
 

