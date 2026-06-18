package com.finanzapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Finanz API service.
 *
 * <p>Spring Boot REST API for personal budget management on port {@code 8080}.
 * Backed by an embedded H2 database with file persistence ({@code ./data/finanz-api}).
 * Exposes endpoints for recording transactions, querying history, and retrieving
 * balance and monthly summary figures.</p>
 *
 * <p>This module is the HTTP-layer evolution of {@code finanz-core}: the same
 * domain logic, delivered over REST instead of a CLI.</p>
 *
 * @see com.finanzapi.controller.BudgetController
 * @see com.finanzapi.service.BudgetService
 */
@SpringBootApplication
public class FinanzApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanzApiApplication.class, args);
	}

}
