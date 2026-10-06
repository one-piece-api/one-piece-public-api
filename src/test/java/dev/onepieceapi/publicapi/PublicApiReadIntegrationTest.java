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

/** The read API end to end: real views, real HTTP, as the cluster would serve them. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiReadIntegrationTest extends PublishedDatabaseTest {

	/** Written with a macron, which the slug drops. */
	private static final String RYU_RYU_NO_MI = "Ry" + (char) 0x16B + " Ry" + (char) 0x16B + " no Mi";

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
	void indexesTheApi() {
		get("/v1").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.languages")
			.isEqualTo("v1/languages")
			.jsonPath("$.resources.devil-fruit-types")
			.isEqualTo("v1/{lang}/devil-fruit-types");
	}

	@Test
	void listsThePublicLanguages() {
		get("/v1/languages").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.length()")
			.isEqualTo(2)
			.jsonPath("$[0].code")
			.isEqualTo("en")
			.jsonPath("$[0].name")
			.isEqualTo("English")
			.jsonPath("$[1].name")
			.isEqualTo("Italiano");
	}

	@Test
	void servesADetailByIdAndBySlugInTheLanguageOfThePath() {
		UUID id = this.fixture.publish(RYU_RYU_NO_MI, Translation.of("it", "Drago"), Translation.of("en", "Dragon"));

		get("/v1/it/devil-fruit-types/" + id).expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.id")
			.isEqualTo(id.toString())
			.jsonPath("$.slug")
			.isEqualTo("ryu-ryu-no-mi")
			.jsonPath("$.language")
			.isEqualTo("it")
			.jsonPath("$.name")
			.isEqualTo("Drago")
			.jsonPath("$.advantages")
			.isEmpty()
			.jsonPath("$.publishedAt")
			.isEqualTo("2026-10-02T14:31:07Z");
		get("/v1/en/devil-fruit-types/ryu-ryu-no-mi").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.name")
			.isEqualTo("Dragon");
	}

	@Test
	void redirectsAnOldSlugToTheCurrentOne() {
		UUID id = this.fixture.publish("Old Name", Translation.of("it", "A"), Translation.of("en", "A"));
		this.fixture.republishWithRomaji(id, "New Name", Translation.of("it", "A"), Translation.of("en", "A"));

		get("/v1/it/devil-fruit-types/old-name").expectStatus()
			.isEqualTo(HttpStatus.MOVED_PERMANENTLY)
			.expectHeader()
			.valueEquals(HttpHeaders.LOCATION, "new-name");
		get("/v1/it/devil-fruit-types/new-name").expectStatus().isOk();
	}

	@Test
	void answers404ForWhatIsNotOnline() {
		UUID retired = this.fixture.publish("Gone", Translation.of("it", "G"), Translation.of("en", "G"));
		this.fixture.retire(retired);

		get("/v1/it/devil-fruit-types/" + retired).expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("CONTENT_NOT_FOUND");
		get("/v1/it/devil-fruit-types/gone").expectStatus().isNotFound();
		get("/v1/it/devil-fruit-types/" + UUID.randomUUID()).expectStatus().isNotFound();
		get("/v1/xx/devil-fruit-types").expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("LANGUAGE_NOT_AVAILABLE");
		get("/v1/it/nothing").expectStatus().isNotFound().expectBody().jsonPath("$.errorCode").isEqualTo("NOT_FOUND");
	}

	@Test
	void listsPagedSortedAndSearched() {
		this.fixture.publish("Zoan", Translation.of("it", "Bestia"), Translation.of("en", "Beast"));
		this.fixture.publish("Logia", Translation.of("it", "Elemento"), Translation.of("en", "Element"));
		this.fixture.publish("Paramecia", Translation.of("it", "Corpo"), Translation.of("en", "Body"));

		get("/v1/it/devil-fruit-types?size=2").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.content[0].name")
			.isEqualTo("Bestia")
			.jsonPath("$.content[1].name")
			.isEqualTo("Corpo")
			.jsonPath("$.totalElements")
			.isEqualTo(3)
			.jsonPath("$.totalPages")
			.isEqualTo(2);
		get("/v1/it/devil-fruit-types?sort=romaji,desc&size=1").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.content[0].romaji")
			.isEqualTo("Zoan");
		get("/v1/it/devil-fruit-types?q=ELEM").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(1)
			.jsonPath("$.content[0].romaji")
			.isEqualTo("Logia");
		get("/v1/it/devil-fruit-types?q=100%25").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(0);
		get("/v1/it/devil-fruit-types?page=5").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.content.length()")
			.isEqualTo(0);
		get("/v1/it/devil-fruit-types?size=1000").expectStatus().isOk().expectBody().jsonPath("$.size").isEqualTo(100);
	}

	@Test
	void rejectsBadListParameters() {
		for (String bad : new String[] { "page=-1", "size=0", "sort=password", "sort=name,sideways", "sort=name,asc,x",
				"q=" + "a".repeat(101) }) {
			get("/v1/it/devil-fruit-types?" + bad).expectStatus()
				.isBadRequest()
				.expectBody()
				.jsonPath("$.errorCode")
				.isEqualTo("VALIDATION_FAILED");
		}
		get("/v1/it/devil-fruit-types?unknown=1").expectStatus().isOk();
	}

	@Test
	void listsOnlyTheVersionOnlineOfEachContent() {
		UUID id = this.fixture.publish("First", Translation.of("it", "A"), Translation.of("en", "A"));
		this.fixture.republishWithRomaji(id, "Second", Translation.of("it", "B"), Translation.of("en", "B"));

		get("/v1/it/devil-fruit-types").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(1)
			.jsonPath("$.content[0].name")
			.isEqualTo("B");
	}

	@Test
	void marksEveryAnswerNosniffAndNamesNoServer() {
		for (String uri : new String[] { "/v1", "/v1/xx/devil-fruit-types" }) {
			get(uri).expectHeader()
				.valueEquals("X-Content-Type-Options", "nosniff")
				.expectHeader()
				.doesNotExist(HttpHeaders.SERVER);
		}
	}

	@Test
	void allowsOnlyReads() {
		this.client.post().uri("/v1/languages").exchange().expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
		this.client.head().uri("/v1/languages").exchange().expectStatus().isOk();
	}

	@Test
	void answersCrossOriginReadsAndPreflights() {
		this.client.get()
			.uri("/v1/languages")
			.header(HttpHeaders.ORIGIN, "https://example.org")
			.exchange()
			.expectHeader()
			.valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		this.client.options()
			.uri("/v1/languages")
			.header(HttpHeaders.ORIGIN, "https://example.org")
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.valueEquals(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "86400");
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
