package com.garage.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
/**
 * Application: Entry point to bootstrap spring application
 */
public class Application {

	private static final Logger LOG = LoggerFactory.getLogger(Application.class);

	/**
	 * Application main method
	 * @param args
	 * 			main arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * CommandLineRunner to allow application to run from command line
	 * @param ctx
	 * 			the spring application context
	 * @return CommandLineRunner instance
	 */
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			LOG.info("### Application started successully ###");
		};
	}

}
