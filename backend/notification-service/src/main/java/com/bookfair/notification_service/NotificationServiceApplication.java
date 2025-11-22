package com.bookfair.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.bookfair.notification_service.config.UserSyncKafkaProperties;

@SpringBootApplication
@EnableConfigurationProperties(UserSyncKafkaProperties.class)
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
