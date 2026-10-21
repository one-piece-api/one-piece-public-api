package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The affiliations end to end (Affiliation plan A2): real views, real HTTP. Seeded for
 * every test, in Italian and English: Mugiwara no Ichimi online with its emblem and the
 * chapter of its first appearance; Kaigun and Baroque Works online without an emblem;
 * Akagami Kaizokudan in draft, with an emblem of its own.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiAffiliationIntegrationTest extends PublishedDatabaseTest {

	private static final byte[] PNG_BYTES = { (byte) 0x89, 0x50, 0x4E, 0x47, 4, 5, 6 };

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID mugiwara;

	private UUID chapter;

	private String emblem;

	private String draftEmblem;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID saga = this.fixture.publishSaga(1, Translation.of("it", "Saga"), Translation.of("en", "Saga"));
		UUID arc = this.fixture.publishArc(1, saga, Translation.of("it", "Arco"), Translation.of("en", "Arc"));
		this.chapter = this.fixture.publishChapter(1, arc, null, null, null, Translation.of("it", "Romance Dawn"),
				Translation.of("en", "Romance Dawn"));
		this.emblem = this.fixture.image('e', PNG_BYTES);
		this.draftEmblem = this.fixture.image('f', PNG_BYTES);
		this.mugiwara = this.fixture.publishAffiliation("Mugiwara no Ichimi", this.emblem,
				Translation.of("it", "Pirati di Cappello di Paglia"), Translation.of("en", "Straw Hat Pirates"));
		this.fixture.firstAppearance(this.mugiwara, this.chapter);
		this.fixture.publishAffiliation("Kaigun", null, Translation.of("it", "Marina"), Translation.of("en", "Navy"));
		this.fixture.publishAffiliation("Baroque Works", null, Translation.of("it", "Baroque Works"),
				Translation.of("en", "Baroque Works"));
		this.fixture.draftAffiliation("Akagami Kaizokudan", this.draftEmblem, Translation.of("it", "Pirati del Rosso"),
				Translation.of("en", "Red Hair Pirates"));
	}

	@Test
	void indexesTheAffiliations() {
		ok("/v1").jsonPath("$.resources.affiliations").isEqualTo("v1/{lang}/affiliations");
	}

	@Test
	void listsTheAffiliationsOnlineByNameInTheLanguageOfThePath() {
		ok("/v1/it/affiliations").jsonPath("$.totalElements")
			.isEqualTo(3)
			.jsonPath("$.content[*].name")
			.isEqualTo(List.of("Baroque Works", "Marina", "Pirati di Cappello di Paglia"))
			.jsonPath("$.content[2].id")
			.isEqualTo(this.mugiwara.toString())
			.jsonPath("$.content[2].slug")
			.isEqualTo("mugiwara-no-ichimi")
			.jsonPath("$.content[2].romaji")
			.isEqualTo("Mugiwara no Ichimi")
			.jsonPath("$.content[2].image")
			.isEqualTo("v1/images/" + this.emblem + ".png")
			.jsonPath("$.content[0].image")
			.isEmpty()
			.jsonPath("$.content[2].description")
			.doesNotExist();
		ok("/v1/en/affiliations").jsonPath("$.content[*].name")
			.isEqualTo(List.of("Baroque Works", "Navy", "Straw Hat Pirates"));
	}

	@Test
	void sortsAndSearchesWithTheCommonFields() {
		ok("/v1/en/affiliations?sort=romaji").jsonPath("$.content[*].romaji")
			.isEqualTo(List.of("Baroque Works", "Kaigun", "Mugiwara no Ichimi"));
		ok("/v1/en/affiliations?sort=name,desc").jsonPath("$.content[0].name").isEqualTo("Straw Hat Pirates");
		ok("/v1/en/affiliations?q=ichimi").jsonPath("$.content[*].name").isEqualTo(List.of("Straw Hat Pirates"));
		ok("/v1/it/affiliations?q=marina").jsonPath("$.content[*].romaji").isEqualTo(List.of("Kaigun"));
		get("/v1/en/affiliations?sort=amount").expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("VALIDATION_FAILED");
	}

	@Test
	void showsTheDetailBySlugOrIdWithEmblemAndFirstAppearance() {
		for (String idOrSlug : new String[] { "mugiwara-no-ichimi", this.mugiwara.toString() }) {
			ok("/v1/it/affiliations/" + idOrSlug).jsonPath("$.id")
				.isEqualTo(this.mugiwara.toString())
				.jsonPath("$.slug")
				.isEqualTo("mugiwara-no-ichimi")
				.jsonPath("$.romaji")
				.isEqualTo("Mugiwara no Ichimi")
				.jsonPath("$.language")
				.isEqualTo("it")
				.jsonPath("$.name")
				.isEqualTo("Pirati di Cappello di Paglia")
				.jsonPath("$.description")
				.isEqualTo("Description of Pirati di Cappello di Paglia")
				.jsonPath("$.image")
				.isEqualTo("v1/images/" + this.emblem + ".png")
				.jsonPath("$.firstAppearance.chapter.id")
				.isEqualTo(this.chapter.toString())
				.jsonPath("$.firstAppearance.chapter.number")
				.isEqualTo(1)
				.jsonPath("$.firstAppearance.chapter.title")
				.isEqualTo("Romance Dawn")
				.jsonPath("$.firstAppearance.episode")
				.isEmpty()
				.jsonPath("$.publishedAt")
				.isNotEmpty();
		}
		ok("/v1/en/affiliations/kaigun").jsonPath("$.image").isEmpty().jsonPath("$.firstAppearance").isEmpty();
	}

	@Test
	void answers404ForAnUnknownOrDraftAffiliationAndRedirectsAnOldSlug() {
		for (String uri : new String[] { "/v1/en/affiliations/akagami-kaizokudan", "/v1/en/affiliations/unknown",
				"/v1/en/affiliations/" + UUID.randomUUID() }) {
			get(uri).expectStatus().isNotFound().expectBody().jsonPath("$.errorCode").isEqualTo("CONTENT_NOT_FOUND");
		}

		this.fixture.republishAffiliation(this.mugiwara, "Mugiwara Kaizokudan",
				Translation.of("en", "Straw Hat Pirates"));

		get("/v1/en/affiliations/mugiwara-no-ichimi").expectStatus()
			.isEqualTo(HttpStatus.MOVED_PERMANENTLY)
			.expectHeader()
			.valueEquals(HttpHeaders.LOCATION, "mugiwara-kaizokudan");
		ok("/v1/en/affiliations/mugiwara-kaizokudan").jsonPath("$.romaji").isEqualTo("Mugiwara Kaizokudan");
	}

	@Test
	void servesTheEmblemOnlyWhileItsAffiliationIsOnline() {
		byte[] body = get("/v1/images/" + this.emblem + ".png").expectStatus()
			.isOk()
			.expectHeader()
			.contentType(MediaType.IMAGE_PNG)
			.expectBody(byte[].class)
			.returnResult()
			.getResponseBody();
		assertThat(body).isEqualTo(PNG_BYTES);
		get("/v1/images/" + this.draftEmblem + ".png").expectStatus().isNotFound();

		this.fixture.retire(this.mugiwara);

		get("/v1/images/" + this.emblem + ".png").expectStatus().isNotFound();
		ok("/v1/en/affiliations").jsonPath("$.totalElements").isEqualTo(2);
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return get(uri).expectStatus().isOk().expectBody();
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
