package com.example.PlugLess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class PlugLessApplication {

	private static final Logger log = LoggerFactory.getLogger(PlugLessApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(PlugLessApplication.class, args);
		Environment env = ctx.getEnvironment();
		String uri = env.getProperty("spring.data.mongodb.uri", "(not set)");
		String db = env.getProperty("spring.data.mongodb.database", "(not set)");
		log.info("========================================");
		log.info("ACTIVE PROFILES : {}", String.join(", ", env.getActiveProfiles()));
		log.info("MONGODB URI     : {}", uri.replaceAll(":([^@/]+)@", ":<hidden>@"));
		log.info("MONGODB DATABASE: {}", db);
		log.info("========================================");
	}
}
