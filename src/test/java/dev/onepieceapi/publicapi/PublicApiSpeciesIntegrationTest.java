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
 * The species end to end (Species plan S2): real views, real HTTP. Seeded for every test,
 * in Italian and English: Ningen online with its picture and the chapter of its first
 * appearance; Gyojin and Mink online without a picture; Kyojin in draft, with a picture
 * of its own.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiSpeciesIntegrationTest extends PublishedDatabaseTest {

	private static final byte[] PNG_BYTES = { (byte) 0x89, 0x50, 0x4E, 0x47, 4, 5, 6 };

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID humans;

	private UUID chapter;

	private String picture;

	private String draftPicture;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID saga = this.fixture.publishSaga(1, Translation.of("it", "Saga"), Translation.of("en", "Saga"));
		UUID arc = this.fixture.publishArc(1, saga, Translation.of("it", "Arco"), Translation.of("en", "Arc"));
		this.chapter = this.fixture.publishChapter(1, arc, null, null, null, Translation.of("it", "Romance Dawn"),
				Translation.of("en", "Romance Dawn"));
		this.picture = this.fixture.image('e', PNG_BYTES);
		this.draftPicture = this.fixture.image('f', PNG_BYTES);
		this.humans = this.fixture.publishSpecies("Ningen", this.picture, Translation.of("it", "Umani"),
				Translation.of("en", "Humans"));
		this.fixture.firstAppearance(this.humans, this.chapter);
		this.fixture.publishSpecies("Gyojin", null, Translation.of("it", "Uomini-pesce"),
				Translation.of("en", "Fish-Men"));
		this.fixture.publishSpecies("Mink", null, Translation.of("it", "Mink"), Translation.of("en", "Mink"));
		this.fixture.draftSpecies("Kyojin", this.draftPicture, Translation.of("it", "Giganti"),
				Translation.of("en", "Giants"));
	}

	@Test
	void indexesTheSpecies() {
		ok("/v1").jsonPath("$.resources.species").isEqualTo("v1/{lang}/species");
	}

	@Test
	void listsTheSpeciesOnlineByNameInTheLanguageOfThePath() {
		ok("/v1/it/species").jsonPath("$.totalElements")
			.isEqualTo(3)
			.jsonPath("$.content[*].name")
			.isEqualTo(List.of("Mink", "Umani", "Uomini-pesce"))
			.jsonPath("$.content[1].id")
			.isEqualTo(this.humans.toString())
			.jsonPath("$.content[1].slug")
			.isEqualTo("ningen")
			.jsonPath("$.content[1].romaji")
			.isEqualTo("Ningen")
			.jsonPath("$.content[1].image")
			.isEqualTo("v1/images/" + this.picture + ".png")
			.jsonPath("$.content[0].image")
			.isEmpty()
			.jsonPath("$.content[1].description")
			.doesNotExist();
		ok("/v1/en/species").jsonPath("$.content[*].name").isEqualTo(List.of("Fish-Men", "Humans", "Mink"));
	}

	@Test
	void sortsAndSearchesWithTheCommonFields() {
		ok("/v1/en/species?sort=romaji").jsonPath("$.content[*].romaji").isEqualTo(List.of("Gyojin", "Mink", "Ningen"));
		ok("/v1/en/species?sort=name,desc").jsonPath("$.content[0].name").isEqualTo("Mink");
		ok("/v1/en/species?q=ningen").jsonPath("$.content[*].name").isEqualTo(List.of("Humans"));
		ok("/v1/it/species?q=pesce").jsonPath("$.content[*].romaji").isEqualTo(List.of("Gyojin"));
		get("/v1/en/species?sort=amount").expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("VALIDATION_FAILED");
	}

	@Test
	void showsTheDetailBySlugOrIdWithPictureAndFirstAppearance() {
		for (String idOrSlug : new String[] { "ningen", this.humans.toString() }) {
			ok("/v1/it/species/" + idOrSlug).jsonPath("$.id")
				.isEqualTo(this.humans.toString())
				.jsonPath("$.slug")
				.isEqualTo("ningen")
				.jsonPath("$.romaji")
				.isEqualTo("Ningen")
				.jsonPath("$.language")
				.isEqualTo("it")
				.jsonPath("$.name")
				.isEqualTo("Umani")
				.jsonPath("$.description")
				.isEqualTo("Description of Umani")
				.jsonPath("$.image")
				.isEqualTo("v1/images/" + this.picture + ".png")
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
		ok("/v1/en/species/gyojin").jsonPath("$.image").isEmpty().jsonPath("$.firstAppearance").isEmpty();
	}

	@Test
	void answers404ForAnUnknownOrDraftSpeciesAndRedirectsAnOldSlug() {
		for (String uri : new String[] { "/v1/en/species/kyojin", "/v1/en/species/unknown",
				"/v1/en/species/" + UUID.randomUUID() }) {
			get(uri).expectStatus().isNotFound().expectBody().jsonPath("$.errorCode").isEqualTo("CONTENT_NOT_FOUND");
		}

		this.fixture.republishSpecies(this.humans, "Ningen Kai", Translation.of("en", "Humans"));

		get("/v1/en/species/ningen").expectStatus()
			.isEqualTo(HttpStatus.MOVED_PERMANENTLY)
			.expectHeader()
			.valueEquals(HttpHeaders.LOCATION, "ningen-kai");
		ok("/v1/en/species/ningen-kai").jsonPath("$.romaji").isEqualTo("Ningen Kai");
	}

	@Test
	void servesThePictureOnlyWhileItsSpeciesIsOnline() {
		byte[] body = get("/v1/images/" + this.picture + ".png").expectStatus()
			.isOk()
			.expectHeader()
			.contentType(MediaType.IMAGE_PNG)
			.expectBody(byte[].class)
			.returnResult()
			.getResponseBody();
		assertThat(body).isEqualTo(PNG_BYTES);
		get("/v1/images/" + this.draftPicture + ".png").expectStatus().isNotFound();

		this.fixture.retire(this.humans);

		get("/v1/images/" + this.picture + ".png").expectStatus().isNotFound();
		ok("/v1/en/species").jsonPath("$.totalElements").isEqualTo(2);
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return get(uri).expectStatus().isOk().expectBody();
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
