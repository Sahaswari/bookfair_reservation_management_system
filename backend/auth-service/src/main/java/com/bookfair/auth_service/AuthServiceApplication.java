package com.bookfair.auth_service;

import com.bookfair.auth_service.config.JwtProperties;
import com.bookfair.auth_service.config.KafkaProperties;
import com.bookfair.auth_service.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, SecurityProperties.class, KafkaProperties.class})
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
