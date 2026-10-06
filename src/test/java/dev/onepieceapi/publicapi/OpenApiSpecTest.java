package dev.onepieceapi.publicapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Snapshot test for the committed OpenAPI contract: the spec generated from the running
 * application's controllers must match {@code openapi/openapi.yaml} exactly, so an API
 * change cannot reach main without its contract (and the Bruno collection built from it)
 * being regenerated and reviewed alongside. {@code ./gradlew updateOpenApiSpec} rewrites
 * the file instead of asserting.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class OpenApiSpecTest extends PublishedDatabaseTest {

	private static final Path SPEC = Path.of("openapi", "openapi.yaml");

	@LocalServerPort
	private int serverPort;

	@Test
	void committedSpecMatchesTheControllers() throws Exception {
		String generated = RestTestClient.bindToServer()
			.baseUrl("http://localhost:" + this.serverPort)
			.build()
			.get()
			.uri("/v3/api-docs.yaml")
			.exchange()
			.expectStatus()
			.isOk()
			.returnResult(String.class)
			.getResponseBody();

		if (Boolean.getBoolean("openapi.update")) {
			Files.createDirectories(SPEC.getParent());
			Files.writeString(SPEC, generated);
			return;
		}
		assertThat(Files.exists(SPEC)).as("%s is missing - run ./gradlew updateOpenApiSpec", SPEC).isTrue();
		assertThat(normalized(generated)).as("%s is stale - run ./gradlew updateOpenApiSpec", SPEC)
			.isEqualTo(normalized(Files.readString(SPEC)));
	}

	/** Ignores line-ending differences introduced by a Windows checkout. */
	private static String normalized(String yaml) {
		return yaml.replace("\r\n", "\n");
	}

}
