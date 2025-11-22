package com.bookfair.reservation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for Reservation Service
 * Microservice for managing stall reservations at book fair events
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.bookfair.reservation_service.repository")
@EntityScan(basePackages = "com.bookfair.reservation_service.entity")
@ComponentScan(basePackages = "com.bookfair.reservation_service")
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

}
