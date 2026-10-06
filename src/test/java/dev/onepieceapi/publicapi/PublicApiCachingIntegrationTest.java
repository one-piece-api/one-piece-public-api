package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The cache policy and the conditional requests (plan D13), on real views and real HTTP.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiCachingIntegrationTest extends PublishedDatabaseTest {

	private static final String LIST = "/v1/it/devil-fruit-types";

	/** Spring writes the directives in its own order; the meaning is plan D13's. */
	private static final String FRESH = "max-age=60, public, stale-while-revalidate=60";

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
	}

	@Test
	void sendsTheCacheControlOfEachStatus() {
		UUID id = this.fixture.publish("Old", Translation.of("it", "A"), Translation.of("en", "A"));
		this.fixture.republishWithRomaji(id, "New", Translation.of("it", "A"), Translation.of("en", "A"));

		assertThat(cacheControlOf(LIST)).isEqualTo(FRESH);
		assertThat(cacheControlOf(LIST + "/new")).isEqualTo(FRESH);
		assertThat(cacheControlOf("/v1")).isEqualTo(FRESH);
		assertThat(cacheControlOf(LIST + "/old")).isEqualTo("max-age=3600, public");
		assertThat(cacheControlOf(LIST + "/nothing")).isEqualTo("max-age=30, public");
		assertThat(cacheControlOf("/nowhere")).isEqualTo("max-age=30, public");
		assertThat(cacheControlOf(LIST + "?page=-1")).isEqualTo("no-store");
	}

	@Test
	void answers304WithoutBodyToACurrentETag() {
		this.fixture.publish("Zoan", Translation.of("it", "Bestia"), Translation.of("en", "Beast"));
		String eTag = eTagOf(LIST);

		assertThat(eTag).startsWith("W/\"");
		for (String current : new String[] { eTag, "\"other\", " + eTag }) {
			this.client.get()
				.uri(LIST)
				.header(HttpHeaders.IF_NONE_MATCH, current)
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.NOT_MODIFIED)
				.expectHeader()
				.valueEquals(HttpHeaders.ETAG, eTag)
				.expectHeader()
				.valueEquals(HttpHeaders.CACHE_CONTROL, FRESH)
				.expectBody()
				.isEmpty();
		}
	}

	@Test
	void answers200ToAStaleETag() {
		this.fixture.publish("Zoan", Translation.of("it", "Bestia"), Translation.of("en", "Beast"));

		this.client.get()
			.uri(LIST)
			.header(HttpHeaders.IF_NONE_MATCH, "W/\"0\"")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(1);
	}

	@Test
	void changesTheETagWhenWhatIsOnlineChanges() {
		String empty = eTagOf(LIST);
		UUID id = this.fixture.publish("Zoan", Translation.of("it", "Bestia"), Translation.of("en", "Beast"));
		String published = eTagOf(LIST);
		this.fixture.retire(id);
		String retired = eTagOf(LIST);

		assertThat(published).isNotEqualTo(empty);
		assertThat(retired).isNotEqualTo(published);
		assertThat(eTagOf("/v1/languages")).isEqualTo(retired);
	}

	@Test
	void keepsTheSameETagWhileNothingChanges() {
		this.fixture.publish("Zoan", Translation.of("it", "Bestia"), Translation.of("en", "Beast"));

		assertThat(eTagOf(LIST)).isEqualTo(eTagOf(LIST + "?q=zoan"));
	}

	private String cacheControlOf(String uri) {
		return this.client.get().uri(uri).exchange().returnResult().getResponseHeaders().getCacheControl();
	}

	private String eTagOf(String uri) {
		return this.client.get().uri(uri).exchange().returnResult().getResponseHeaders().getETag();
	}

}
