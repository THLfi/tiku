package fi.thl.pivot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages={"fi.thl.pivot"})
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
      builder.sources(Application.class);
      // Only applies when app is run by deploying WAR file to Tomcat
      builder.properties("spring.config.location=classpath:/,file:${catalina.base}/conf/tiku/");
      return builder;
	}

}