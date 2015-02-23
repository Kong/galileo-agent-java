# Analytics Java Agent


Analytics Java Agent is custom servlet filter which intercepts the request and response and sends it to API Analytics server asynchronously to generate analytics information.


# Installation 

	
# With Maven


```xml

	<dependency>
  		<groupId>com.mashape.analytics.agent</groupId>
  		<artifactId>analytics-java-agent</artifactId>
  		<version>1.0.0-alpha-1</version>
	</dependency>

``` 

# Without Maven



Application depends on javax.servlet-api-3.0.1, jeromq-0.3.4, gson-1.2.17, log4j-1.2.17. For testing it depends on jmockit-1.7 and junit-4.12
	
You can download the analytics jar from 
<https://oss.sonatype.org/content/repositories/releases/com/mashape/analytics/agent/analytics-java-agent/1.0.0-alpha-1/analytics-java-agent-1.0.0-alpha-1.jar>
	
or clone the project from github
<https://github.com/Mashape/analytics-java-agent/>
	
Dependencies

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
			<version>4.12</version>
			<scope>test</scope>
		</dependency>
		
```


# Configuration for Server

Filter has been tested on tomcat and should work with Jetty, Jboss and other servers supporting http servlet api. 
To use the filter you would need to add Analytics filter to web descriptor and set few environment variables in the server.

Add following environment variable to the server
     
     ANALYTICS_TOKEN = You Api analytics token from [](http://www.apianalyitics.com "Api Analytics") 
     SOCKET_POOL_SIZE_MIN = Minimun number of sockets to opened for connection to analytics server, default is 10
     SOCKET_POOL_SIZE_MAX = Maximum number of sockets pool should keep, default is 20
     THREAD_POOL_SIZE = Number of thread in the pool handling data transfer to analytics server, default is 2* # of processor 
	
Update web.xml on server

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
	
     



	


 
 

