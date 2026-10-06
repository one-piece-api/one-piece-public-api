package dev.onepieceapi.publicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The skeleton as deployed: connected as public_api_reader to the published views, ready
 * only then, its actuator on the management port and nowhere else.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiApplicationIntegrationTest extends PublishedDatabaseTest {

	@Autowired
	private JdbcClient jdbc;

	@LocalServerPort
	private int serverPort;

	@LocalManagementPort
	private int managementPort;

	@Test
	void readsThePublishedViewsAsTheReader() {
		assertThat(this.jdbc.sql("SELECT current_user").query(String.class).single()).isEqualTo("public_api_reader");
		assertThat(this.jdbc.sql("SELECT revision FROM published.revision").query(Long.class).single()).isPositive();
	}

	@Test
	void isReadyOnTheManagementPort() {
		client(this.managementPort).get()
			.uri("/actuator/health/readiness")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.status")
			.isEqualTo("UP");
	}

	@Test
	void keepsTheActuatorOffThePublicPort() {
		client(this.serverPort).get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus()
			.value((status) -> assertThat(HttpStatusCode.valueOf(status).is2xxSuccessful()).isFalse());
	}

	private static RestTestClient client(int port) {
		return RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

}
