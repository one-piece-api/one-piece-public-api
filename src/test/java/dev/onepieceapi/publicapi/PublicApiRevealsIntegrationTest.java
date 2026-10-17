package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

/**
 * The Devil Fruits as the reader knows them at chapter N (plan of the timeline, T13,
 * TL6). Seeded in Italian: chapters 1, 50, 100 and 101; Paramecia met at chapter 1, Zoan
 * at 100 with "Mitologico" (no first appearance: met with its type) and "Antico" (101).
 * Gomu Gomu is truly "Nika", Zoan Mitologico: first known as "Gom Gom", Paramecia, with
 * an unknown description, learned as "Gomma" at 50, the truth at 100. Inu Inu, met at 1,
 * is a Zoan Antico.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiRevealsIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID arc = this.fixture.publishArc(1, this.fixture.publishSaga(1, Translation.of("it", "Mare Orientale")),
				Translation.of("it", "Romance Dawn"));
		UUID chapter1 = chapter(1, arc);
		UUID chapter50 = chapter(50, arc);
		UUID chapter100 = chapter(100, arc);
		UUID chapter101 = chapter(101, arc);
		UUID paramecia = this.fixture.publish("Paramecia", Translation.of("it", "Paramecia"));
		UUID zoan = this.fixture.publish("Zoan", Translation.of("it", "Zoan"));
		this.fixture.firstAppearance(paramecia, chapter1);
		this.fixture.firstAppearance(zoan, chapter100);
		UUID mythical = this.fixture.subcategory(zoan, 0, Translation.of("it", "Mitologico"));
		UUID ancient = this.fixture.subcategory(zoan, 1, Translation.of("it", "Antico"));
		this.fixture.subcategoryFirstAppearance(ancient, chapter101);

		UUID gomu = this.fixture.publishFruitOfSubcategory("Hito Hito no Mi, Model Nika", zoan, mythical,
				new Translation("it", "Nika", "Il frutto del dio del sole", null, null));
		this.fixture.firstAppearance(gomu, chapter1);
		this.fixture.initialKnowledge(gomu, "Gomu Gomu no Mi", paramecia,
				new Translation("it", "Gom Gom", null, null, null));
		this.fixture.reveal(gomu, 0, chapter50, List.of("DESCRIPTION"), null, null,
				new Translation("it", null, "Gomma", null, null));
		this.fixture.reveal(gomu, 1, chapter100, List.of("NAME", "ROMAJI", "TYPE", "DESCRIPTION"), null, null,
				new Translation("it", null, null, null, null));
		UUID inu = this.fixture.publishFruitOfSubcategory("Inu Inu no Mi", zoan, ancient,
				Translation.of("it", "Il Cane"));
		this.fixture.firstAppearance(inu, chapter1);
	}

	@Test
	void withoutTheFilterTheTruthIsShown() {
		get("/v1/it/devil-fruits/hito-hito-no-mi-model-nika").expectBody()
			.jsonPath("$.name")
			.isEqualTo("Nika")
			.jsonPath("$.type.slug")
			.isEqualTo("zoan")
			.jsonPath("$.subcategory.name")
			.isEqualTo("Mitologico");
	}

	@Test
	void aDetailShowsWhatTheReaderKnowsAtFirstAppearanceWithUnknownsAsNull() {
		get("/v1/it/devil-fruits/hito-hito-no-mi-model-nika?asOfChapter=1").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.name")
			.isEqualTo("Gom Gom")
			.jsonPath("$.romaji")
			.isEqualTo("Gomu Gomu no Mi")
			.jsonPath("$.slug")
			.doesNotExist()
			.jsonPath("$.type.slug")
			.isEqualTo("paramecia")
			.jsonPath("$.subcategory")
			.doesNotExist()
			.jsonPath("$.description")
			.doesNotExist();
	}

	@Test
	void aRevealAppliesFromItsChapterInclusive() {
		get("/v1/it/devil-fruits/hito-hito-no-mi-model-nika?asOfChapter=49").expectBody()
			.jsonPath("$.description")
			.doesNotExist();
		get("/v1/it/devil-fruits/hito-hito-no-mi-model-nika?asOfChapter=50").expectBody()
			.jsonPath("$.description")
			.isEqualTo("Gomma")
			.jsonPath("$.name")
			.isEqualTo("Gom Gom");
		get("/v1/it/devil-fruits/hito-hito-no-mi-model-nika?asOfChapter=100").expectBody()
			.jsonPath("$.name")
			.isEqualTo("Nika")
			.jsonPath("$.slug")
			.isEqualTo("hito-hito-no-mi-model-nika")
			.jsonPath("$.description")
			.isEqualTo("Il frutto del dio del sole")
			.jsonPath("$.type.slug")
			.isEqualTo("zoan")
			.jsonPath("$.subcategory.name")
			.isEqualTo("Mitologico");
	}

	@Test
	void aTypeOrSubcategoryNotMetYetIsNull() {
		get("/v1/it/devil-fruits/inu-inu-no-mi?asOfChapter=99").expectBody()
			.jsonPath("$.name")
			.isEqualTo("Il Cane")
			.jsonPath("$.type")
			.doesNotExist()
			.jsonPath("$.subcategory")
			.doesNotExist();
		get("/v1/it/devil-fruits/inu-inu-no-mi?asOfChapter=100").expectBody()
			.jsonPath("$.type.slug")
			.isEqualTo("zoan")
			.jsonPath("$.subcategory")
			.doesNotExist();
		get("/v1/it/devil-fruits/inu-inu-no-mi?asOfChapter=101").expectBody()
			.jsonPath("$.subcategory.name")
			.isEqualTo("Antico");
	}

	@Test
	void searchFiltersAndOrderUseTheValuesKnownByThen() {
		get("/v1/it/devil-fruits?q=nika&asOfChapter=99").expectBody().jsonPath("$.totalElements").isEqualTo(0);
		get("/v1/it/devil-fruits?q=gomu&asOfChapter=99").expectBody().jsonPath("$.totalElements").isEqualTo(1);
		get("/v1/it/devil-fruits?q=nika").expectBody().jsonPath("$.totalElements").isEqualTo(1);
		get("/v1/it/devil-fruits?type=paramecia&asOfChapter=99").expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(1)
			.jsonPath("$.content[0].name")
			.isEqualTo("Gom Gom")
			.jsonPath("$.content[0].slug")
			.doesNotExist();
		get("/v1/it/devil-fruits?type=paramecia&asOfChapter=100").expectBody().jsonPath("$.totalElements").isEqualTo(0);
		get("/v1/it/devil-fruits?sort=name&asOfChapter=99").expectBody()
			.jsonPath("$.content[0].name")
			.isEqualTo("Gom Gom")
			.jsonPath("$.content[1].name")
			.isEqualTo("Il Cane");
		get("/v1/it/devil-fruits?sort=name").expectBody()
			.jsonPath("$.content[0].name")
			.isEqualTo("Il Cane")
			.jsonPath("$.content[1].name")
			.isEqualTo("Nika");
	}

	@Test
	void aTypeListsItsFruitsAsKnownByThen() {
		get("/v1/it/devil-fruit-types/paramecia?asOfChapter=99").expectBody()
			.jsonPath("$.devilFruits.length()")
			.isEqualTo(1)
			.jsonPath("$.devilFruits[0].name")
			.isEqualTo("Gom Gom")
			.jsonPath("$.devilFruits[0].slug")
			.doesNotExist();
		get("/v1/it/devil-fruit-types/zoan?asOfChapter=100").expectBody()
			.jsonPath("$.devilFruits.length()")
			.isEqualTo(2);
	}

	@Test
	void aTypeListsOnlyTheSubcategoriesMetByThen() {
		get("/v1/it/devil-fruit-types/zoan?asOfChapter=100").expectBody()
			.jsonPath("$.subcategories.length()")
			.isEqualTo(1)
			.jsonPath("$.subcategories[0].name")
			.isEqualTo("Mitologico");
		get("/v1/it/devil-fruit-types/zoan?asOfChapter=101").expectBody()
			.jsonPath("$.subcategories.length()")
			.isEqualTo(2);
		get("/v1/it/devil-fruit-types/zoan").expectBody().jsonPath("$.subcategories.length()").isEqualTo(2);
	}

	private UUID chapter(int number, UUID arc) {
		return this.fixture.publishChapter(number, arc, null, null, null, Translation.of("it", "Capitolo " + number));
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
