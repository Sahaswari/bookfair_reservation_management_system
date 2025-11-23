package com.bookfair.genre_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.bookfair.genre_service.config.UserSyncKafkaProperties;

@SpringBootApplication
@EnableConfigurationProperties(UserSyncKafkaProperties.class)
public class GenreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GenreServiceApplication.class, args);
	}

}
