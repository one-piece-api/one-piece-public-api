package dev.onepieceapi.publicapi;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base of the tests that need content-service's real database: one PostgreSQL container
 * (Testcontainers) for the whole run, migrated by Flyway with content-service's published
 * migrations as the container's owner, then read by this service as public_api_reader, as
 * in the cluster.
 */
public abstract class PublishedDatabaseTest {

	static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.6");

	static {
		POSTGRES.start();
	}

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.password", () -> "reader-test");
		registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
		registry.add("spring.flyway.user", POSTGRES::getUsername);
		registry.add("spring.flyway.password", POSTGRES::getPassword);
	}

}
