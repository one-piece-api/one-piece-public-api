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

/**
 * The spoiler filter {@code asOfChapter} on the Devil Fruits and their types end to end
 * (plan of the timeline, T8, TL5). Seeded for every test, in Italian: chapters 1, 4 and
 * 100 online (chapter 100 with no Italian title); Paramecia first met at chapter 1, Zoan
 * at chapter 100, Logia with no first appearance; Gomu Gomu (Paramecia, chapter 1), Bara
 * Bara (Paramecia, chapter 4) and Inu Inu (Zoan, chapter 100).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiSpoilerFilterIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID paramecia;

	private UUID zoan;

	private UUID inu;

	private UUID chapter100;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID arc = this.fixture.publishArc(1, this.fixture.publishSaga(1, Translation.of("it", "Mare Orientale")),
				Translation.of("it", "Romance Dawn"), Translation.of("en", "Romance Dawn"));
		UUID chapter1 = this.fixture.publishChapter(1, arc, null, null, null, Translation.of("it", "Alba"));
		UUID chapter4 = this.fixture.publishChapter(4, arc, null, null, null, Translation.of("it", "Il capitano"));
		this.chapter100 = this.fixture.publishChapter(100, arc, null, null, null, Translation.of("en", "Legend"));
		this.paramecia = this.fixture.publish("Paramecia", Translation.of("it", "Paramecia"));
		this.zoan = this.fixture.publish("Zoan", Translation.of("it", "Zoan"));
		this.fixture.publish("Logia", Translation.of("it", "Logia"));
		this.fixture.firstAppearance(this.paramecia, chapter1);
		this.fixture.firstAppearance(this.zoan, this.chapter100);
		fruit("Gomu Gomu no Mi", this.paramecia, "Gom Gom", chapter1);
		fruit("Bara Bara no Mi", this.paramecia, "Puzzle Puzzle", chapter4);
		this.inu = fruit("Inu Inu no Mi", this.zoan, "Cane Cane", this.chapter100);
	}

	@Test
	void withoutTheFilterEverythingOnlineIsShown() {
		get("/v1/it/devil-fruits").expectStatus().isOk().expectBody().jsonPath("$.totalElements").isEqualTo(3);
		get("/v1/it/devil-fruit-types").expectStatus().isOk().expectBody().jsonPath("$.totalElements").isEqualTo(3);
		get("/v1/it/devil-fruits/inu-inu-no-mi").expectStatus().isOk();
	}

	@Test
	void listsAndCountsOnlyWhatTheReaderHasMetByThatChapterInclusive() {
		get("/v1/it/devil-fruits?asOfChapter=4").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(2)
			.jsonPath("$.content[0].slug")
			.isEqualTo("gomu-gomu-no-mi")
			.jsonPath("$.content[1].slug")
			.isEqualTo("bara-bara-no-mi");
		get("/v1/it/devil-fruits?asOfChapter=0").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(0);
		get("/v1/it/devil-fruits?asOfChapter=5000").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(3);
	}

	@Test
	void aChapterWithNoTextInTheLanguageStillCountsByItsNumber() {
		get("/v1/it/devil-fruits?asOfChapter=100").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(3);
	}

	@Test
	void aSearchFindsNothingTheReaderHasNotMetYet() {
		get("/v1/it/devil-fruits?q=cane&asOfChapter=99").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(0);
		get("/v1/it/devil-fruits?q=cane").expectStatus().isOk().expectBody().jsonPath("$.totalElements").isEqualTo(1);
	}

	@Test
	void aTypeWithNoFirstAppearanceIsHiddenByTheFilter() {
		get("/v1/it/devil-fruit-types?asOfChapter=100").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(2);
		get("/v1/it/devil-fruit-types/logia?asOfChapter=100").expectStatus().isNotFound();
	}

	@Test
	void aDetailNotMetYetAnswers404ByIdAndBySlug() {
		get("/v1/it/devil-fruits/inu-inu-no-mi?asOfChapter=99").expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("CONTENT_NOT_FOUND");
		get("/v1/it/devil-fruits/" + this.inu + "?asOfChapter=99").expectStatus().isNotFound();
		get("/v1/it/devil-fruit-types/zoan?asOfChapter=99").expectStatus().isNotFound();
		get("/v1/it/devil-fruits/inu-inu-no-mi?asOfChapter=100").expectStatus().isOk();
	}

	@Test
	void aTypeListsOnlyItsFruitsTheReaderHasMet() {
		get("/v1/it/devil-fruit-types/paramecia?asOfChapter=3").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.devilFruits.length()")
			.isEqualTo(1)
			.jsonPath("$.devilFruits[0].slug")
			.isEqualTo("gomu-gomu-no-mi");
	}

	@Test
	void anOldSlugMovesKeepingTheFilterAndAnswers404WhenHidden() {
		this.fixture.republishWithRomaji(this.zoan, "Zoan Kei", Translation.of("it", "Zoan"));
		this.fixture.firstAppearance(this.zoan, this.chapter100);

		get("/v1/it/devil-fruit-types/zoan?asOfChapter=100").expectStatus()
			.isEqualTo(HttpStatus.MOVED_PERMANENTLY)
			.expectHeader()
			.valueEquals(HttpHeaders.LOCATION, "zoan-kei?asOfChapter=100");
		get("/v1/it/devil-fruit-types/zoan?asOfChapter=99").expectStatus().isNotFound();
	}

	@Test
	void refusesAChapterThatIsNotANumberOfZeroOrMore() {
		get("/v1/it/devil-fruits?asOfChapter=-1").expectStatus().isBadRequest();
		get("/v1/it/devil-fruits/gomu-gomu-no-mi?asOfChapter=uno").expectStatus().isBadRequest();
	}

	private UUID fruit(String romaji, UUID typeId, String name, UUID chapterId) {
		UUID id = this.fixture.publishFruit(romaji, typeId, null, Translation.of("it", name));
		this.fixture.firstAppearance(id, chapterId);
		return id;
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
