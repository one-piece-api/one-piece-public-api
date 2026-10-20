package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Poster;
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
 * The bounties end to end (Bounty plan B3): real views, real HTTP. Seeded for every test,
 * in Italian and English: Luffy's chain 30M, 100M, 300M online, with a draft 500M naming
 * the 300M (it does not supersede it); Nami 16M, only alive; Nico Robin 79M, inactive.
 * The 300M has a poster and the chapter of its first appearance.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiBountyIntegrationTest extends PublishedDatabaseTest {

	private static final byte[] PNG_BYTES = { (byte) 0x89, 0x50, 0x4E, 0x47, 7, 8, 9 };

	private static final String LUFFY = "Monkey D. Luffy";

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID luffy300;

	private UUID nami;

	private UUID chapter;

	private String poster;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID saga = this.fixture.publishSaga(1, Translation.of("it", "Saga"), Translation.of("en", "Saga"));
		UUID arc = this.fixture.publishArc(1, saga, Translation.of("it", "Arco"), Translation.of("en", "Arc"));
		this.chapter = this.fixture.publishChapter(435, arc, null, null, null, Translation.of("it", "Capitolo"),
				Translation.of("en", "Chapter"));
		this.poster = this.fixture.image('d', PNG_BYTES);
		UUID luffy30 = bounty(Poster.builder().posterName(LUFFY).amount(30_000_000L).build());
		UUID luffy100 = bounty(Poster.builder().posterName(LUFFY).amount(100_000_000L).previousId(luffy30).build());
		this.luffy300 = bounty(Poster.builder()
			.posterName(LUFFY)
			.amount(300_000_000L)
			.previousId(luffy100)
			.imageId(this.poster)
			.build());
		this.fixture.firstAppearance(this.luffy300, this.chapter);
		bounty(Poster.builder()
			.posterName(LUFFY)
			.amount(500_000_000L)
			.previousId(this.luffy300)
			.status("DRAFT")
			.build());
		this.nami = bounty(Poster.builder().posterName("Nami").amount(16_000_000L).condition("ONLY_ALIVE").build());
		bounty(Poster.builder().posterName("Nico Robin").amount(79_000_000L).inactive(true).build());
	}

	@Test
	void indexesTheBounties() {
		ok("/v1").jsonPath("$.resources.bounties").isEqualTo("v1/{lang}/bounties");
	}

	@Test
	void listsTheBountiesOnlineHighestFirstWithTheirDerivedValidity() {
		ok("/v1/en/bounties").jsonPath("$.totalElements")
			.isEqualTo(5)
			.jsonPath("$.content[*].amount")
			.isEqualTo(List.of(300_000_000, 100_000_000, 79_000_000, 30_000_000, 16_000_000))
			.jsonPath("$.content[*].validity")
			.isEqualTo(List.of("ACTIVE", "SUPERSEDED", "INACTIVE", "SUPERSEDED", "ACTIVE"))
			.jsonPath("$.content[0].id")
			.isEqualTo(this.luffy300.toString())
			.jsonPath("$.content[0].slug")
			.isEqualTo("monkey-d-luffy-300000000")
			.jsonPath("$.content[0].posterName")
			.isEqualTo(LUFFY)
			.jsonPath("$.content[0].condition")
			.isEqualTo("DEAD_OR_ALIVE")
			.jsonPath("$.content[0].image")
			.isEqualTo("v1/images/" + this.poster + ".png")
			.jsonPath("$.content[4].condition")
			.isEqualTo("ONLY_ALIVE")
			.jsonPath("$.content[4].image")
			.isEmpty();
	}

	@Test
	void filtersByValidityAndConditionAndCombinesThem() {
		ok("/v1/en/bounties?validity=SUPERSEDED").jsonPath("$.content[*].amount")
			.isEqualTo(List.of(100_000_000, 30_000_000));
		ok("/v1/en/bounties?validity=INACTIVE").jsonPath("$.content[*].posterName").isEqualTo(List.of("Nico Robin"));
		ok("/v1/en/bounties?condition=ONLY_ALIVE").jsonPath("$.content[*].posterName").isEqualTo(List.of("Nami"));
		ok("/v1/en/bounties?validity=ACTIVE&condition=DEAD_OR_ALIVE").jsonPath("$.content[*].amount")
			.isEqualTo(List.of(300_000_000));
	}

	@Test
	void sortsByAmountAndByPosterNameThenAmountAsANumber() {
		ok("/v1/en/bounties?sort=amount").jsonPath("$.content[0].posterName").isEqualTo("Nami");
		ok("/v1/en/bounties?sort=name").jsonPath("$.content[*].amount")
			.isEqualTo(List.of(30_000_000, 100_000_000, 300_000_000, 16_000_000, 79_000_000));
		ok("/v1/en/bounties?sort=name,desc").jsonPath("$.content[0].posterName").isEqualTo("Nico Robin");
	}

	@Test
	void searchesThePosterNameAndTheKeyText() {
		ok("/v1/en/bounties?q=luffy").jsonPath("$.totalElements").isEqualTo(3);
		ok("/v1/en/bounties?q=16000000").jsonPath("$.content[*].posterName").isEqualTo(List.of("Nami"));
	}

	@Test
	void rejectsAnUnknownValidityOrConditionAndTheAmountSortElsewhere() {
		for (String uri : new String[] { "/v1/en/bounties?validity=WANTED", "/v1/en/bounties?condition=DEAD",
				"/v1/en/devil-fruits?sort=amount", "/v1/en/volumes?sort=amount,desc" }) {
			get(uri).expectStatus().isBadRequest().expectBody().jsonPath("$.errorCode").isEqualTo("VALIDATION_FAILED");
		}
	}

	@Test
	void showsTheDetailWithItsChainNewestFirstFromAnyLinkOfIt() {
		for (String link : new String[] { "monkey-d-luffy-30000000", "monkey-d-luffy-100000000",
				this.luffy300.toString() }) {
			ok("/v1/it/bounties/" + link).jsonPath("$.chain[*].amount")
				.isEqualTo(List.of(300_000_000, 100_000_000, 30_000_000))
				.jsonPath("$.chain[*].validity")
				.isEqualTo(List.of("ACTIVE", "SUPERSEDED", "SUPERSEDED"));
		}
		ok("/v1/it/bounties/monkey-d-luffy-300000000").jsonPath("$.posterName")
			.isEqualTo(LUFFY)
			.jsonPath("$.amount")
			.isEqualTo(300_000_000)
			.jsonPath("$.validity")
			.isEqualTo("ACTIVE")
			.jsonPath("$.language")
			.isEqualTo("it")
			.jsonPath("$.description")
			.isEqualTo("Description of " + LUFFY + " it")
			.jsonPath("$.image")
			.isEqualTo("v1/images/" + this.poster + ".png")
			.jsonPath("$.firstAppearance.chapter.id")
			.isEqualTo(this.chapter.toString())
			.jsonPath("$.firstAppearance.chapter.number")
			.isEqualTo(435)
			.jsonPath("$.firstAppearance.episode")
			.isEmpty()
			.jsonPath("$.publishedAt")
			.isNotEmpty();
	}

	@Test
	void aBountyWithoutPreviousOrSuccessorIsItsOwnChain() {
		ok("/v1/en/bounties/nami-16000000").jsonPath("$.chain[*].id").isEqualTo(List.of(this.nami.toString()));
	}

	@Test
	void answers404ForAnUnknownOrDraftBountyAndRedirectsAnOldSlug() {
		get("/v1/en/bounties/monkey-d-luffy-500000000").expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("CONTENT_NOT_FOUND");
		get("/v1/en/bounties/" + UUID.randomUUID()).expectStatus().isNotFound();

		this.fixture.republishBounty(this.nami,
				Poster.builder().posterName("Nami").amount(66_000_000L).condition("ONLY_ALIVE").build(),
				translations("Nami"));

		get("/v1/en/bounties/nami-16000000").expectStatus()
			.isEqualTo(HttpStatus.MOVED_PERMANENTLY)
			.expectHeader()
			.valueEquals(HttpHeaders.LOCATION, "nami-66000000");
		ok("/v1/en/bounties/nami-66000000").jsonPath("$.amount").isEqualTo(66_000_000);
	}

	@Test
	void servesThePosterOfAnOnlineBountyAsAPng() {
		byte[] body = get("/v1/images/" + this.poster + ".png").expectStatus()
			.isOk()
			.expectHeader()
			.contentType(MediaType.IMAGE_PNG)
			.expectBody(byte[].class)
			.returnResult()
			.getResponseBody();

		assertThat(body).isEqualTo(PNG_BYTES);
	}

	private UUID bounty(Poster seed) {
		return this.fixture.bounty(seed, translations(seed.posterName()));
	}

	/** "Description of NAME LANGUAGE" in Italian and English. */
	private static Translation[] translations(String posterName) {
		return new Translation[] { Translation.of("it", posterName + " it"), Translation.of("en", posterName + " en") };
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return get(uri).expectStatus().isOk().expectBody();
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
