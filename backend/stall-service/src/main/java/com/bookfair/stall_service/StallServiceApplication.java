package com.bookfair.stall_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.bookfair.stall_service.config.UserSyncKafkaProperties;

@SpringBootApplication
@EnableConfigurationProperties(UserSyncKafkaProperties.class)
public class StallServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StallServiceApplication.class, args);
	}

}
