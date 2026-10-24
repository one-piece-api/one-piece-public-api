package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The story structure end to end (story structure plan S9): sagas, arcs, volumes,
 * chapters and episodes, real views, real HTTP. Seeded for every test: sagas 1 and 2;
 * arcs 1 and 2 of saga 1 and arc 3 of saga 2; volumes 1 (with a cover) and 2; chapters 1
 * and 2 (arc 1, volume 1), 3 (arc 1, no volume) and 4 (arc 2, volume 2), plus a draft
 * chapter 99; episodes 1 and 2 (arc 1) and 3 (a filler). Everything in Italian and
 * English.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiStoryIntegrationTest extends PublishedDatabaseTest {

	private static final byte[] WEBP_BYTES = { 0x52, 0x49, 0x46, 0x46, 4, 5, 6 };

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID saga1;

	private UUID saga2;

	private UUID arc1;

	private UUID arc2;

	private UUID volume1;

	private UUID volume2;

	private UUID chapter1;

	private UUID chapter3;

	private UUID chapter4;

	private UUID episode1;

	private UUID episode3;

	private String cover;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		this.saga1 = this.fixture.publishSaga(1, it("Blu Orientale"), en("East Blue"));
		this.saga2 = this.fixture.publishSaga(2, it("Alabasta IT"), en("Alabasta"));
		this.arc1 = this.fixture.publishArc(1, this.saga1, it("Alba"), en("Romance Dawn"));
		this.arc2 = this.fixture.publishArc(2, this.saga1, it("Arancione"), en("Orange Town"));
		this.fixture.publishArc(3, this.saga2, it("Deserto"), en("Desert"));
		this.cover = this.fixture.image('f', WEBP_BYTES);
		this.volume1 = this.fixture.publishVolume(1, "ROMANCE DAWN", this.cover, it("Volume Alba"), en("Volume Dawn"));
		this.volume2 = this.fixture.publishVolume(2, null, null, it("Volume Ciurma"), en("Volume Crew"));
		this.chapter1 = this.fixture.publishChapter(1, this.arc1, this.volume1, "ROMANCE DAWN", 53, it("Alba cap"),
				en("Dawn"));
		UUID chapter2 = this.fixture.publishChapter(2, this.arc1, this.volume1, null, null, it("Cappello cap"),
				en("Straw Hat"));
		this.chapter3 = this.fixture.publishChapter(3, this.arc1, null, null, null, it("Zoro cap"), en("Zoro"));
		this.chapter4 = this.fixture.publishChapter(4, this.arc2, this.volume2, null, null, it("Arancia cap"),
				en("Orange"));
		this.fixture.draftChapter(99, this.arc1, null, null, null, en("Draft"));
		this.episode1 = this.fixture.publishEpisode(1, this.arc1, "ENTRANCE", it("Entrata"), en("Big Entrance"));
		UUID episode2 = this.fixture.publishEpisode(2, this.arc1, null, it("Spadaccino"), en("Swordsman"));
		this.episode3 = this.fixture.publishEpisode(3, null, null, it("Riempitivo"), en("Filler"));

		this.fixture.chapterRelease(this.chapter1, "IT", LocalDate.of(2001, 3, 1));
		this.fixture.chapterRelease(this.chapter1, "JP", LocalDate.of(1997, 7, 22));
		this.fixture.chapterRelease(chapter2, "JP", LocalDate.of(1997, 8, 4));
		this.fixture.chapterRelease(this.chapter3, "JP", LocalDate.of(1997, 8, 11));
		this.fixture.chapterRelease(this.chapter4, "JP", LocalDate.of(1997, 9, 1));
		this.fixture.volumeRelease(this.volume1, "US", LocalDate.of(2003, 6, 3));
		this.fixture.volumeRelease(this.volume1, "JP", LocalDate.of(1997, 12, 24));
		this.fixture.volumeRelease(this.volume1, "IT", LocalDate.of(2002, 5, 1));
		this.fixture.episodeBroadcast(this.episode1, "US", LocalDate.of(2001, 1, 1));
		this.fixture.episodeBroadcast(this.episode1, "JP", LocalDate.of(1999, 10, 20));
		this.fixture.episodeBroadcast(episode2, "JP", LocalDate.of(1999, 11, 17));
	}

	@Test
	void indexesTheStoryResources() {
		ok("/v1").jsonPath("$.resources.sagas")
			.isEqualTo("v1/{lang}/sagas")
			.jsonPath("$.resources.arcs")
			.isEqualTo("v1/{lang}/arcs")
			.jsonPath("$.resources.volumes")
			.isEqualTo("v1/{lang}/volumes")
			.jsonPath("$.resources.chapters")
			.isEqualTo("v1/{lang}/chapters")
			.jsonPath("$.resources.episodes")
			.isEqualTo("v1/{lang}/episodes");
	}

	@Test
	void listsEveryEntityByNumberAscendingWithThePageFields() {
		for (String[] list : new String[][] { { "sagas", "2" }, { "arcs", "3" }, { "volumes", "2" },
				{ "chapters", "4" }, { "episodes", "3" } }) {
			ok("/v1/en/" + list[0]).jsonPath("$.content[0].number")
				.isEqualTo(1)
				.jsonPath("$.content[1].number")
				.isEqualTo(2)
				.jsonPath("$.totalElements")
				.isEqualTo(Integer.parseInt(list[1]))
				.jsonPath("$.page")
				.isEqualTo(0)
				.jsonPath("$.size")
				.isEqualTo(20)
				.jsonPath("$.totalPages")
				.isEqualTo(1);
		}
		ok("/v1/it/sagas").jsonPath("$.content[0].title")
			.isEqualTo("Blu Orientale")
			.jsonPath("$.content[0].slug")
			.isEqualTo("1")
			.jsonPath("$.content[0].id")
			.isEqualTo(this.saga1.toString());
	}

	@Test
	void sortsByNumberDescendingAndByName() {
		ok("/v1/en/sagas?sort=number,desc").jsonPath("$.content[0].number")
			.isEqualTo(2)
			.jsonPath("$.content[1].number")
			.isEqualTo(1);
		ok("/v1/en/sagas?sort=name").jsonPath("$.content[0].title")
			.isEqualTo("Alabasta")
			.jsonPath("$.content[1].title")
			.isEqualTo("East Blue");
		ok("/v1/en/chapters?sort=number,desc").jsonPath("$.content[0].number")
			.isEqualTo(4)
			.jsonPath("$.content[3].number")
			.isEqualTo(1);
		ok("/v1/en/chapters?sort=name").jsonPath("$.content[0].title")
			.isEqualTo("Dawn")
			.jsonPath("$.content[1].title")
			.isEqualTo("Orange")
			.jsonPath("$.content[3].title")
			.isEqualTo("Zoro");
	}

	@Test
	void searchesByNumberAndByTitle() {
		ok("/v1/en/sagas?q=2").jsonPath("$.totalElements").isEqualTo(1).jsonPath("$.content[0].number").isEqualTo(2);
		ok("/v1/en/chapters?q=2").jsonPath("$.totalElements")
			.isEqualTo(1)
			.jsonPath("$.content[0].title")
			.isEqualTo("Straw Hat");
		ok("/v1/en/sagas?q=east").jsonPath("$.totalElements").isEqualTo(1).jsonPath("$.content[0].number").isEqualTo(1);
		ok("/v1/en/episodes?q=entrance").jsonPath("$.totalElements")
			.isEqualTo(1)
			.jsonPath("$.content[0].number")
			.isEqualTo(1);
		ok("/v1/en/arcs?q=nothing-like-this").jsonPath("$.totalElements").isEqualTo(0);
	}

	@Test
	void filtersTheArcsBySagaIdOrNumber() {
		for (String saga : new String[] { this.saga1.toString(), "1" }) {
			ok("/v1/en/arcs?saga=" + saga).jsonPath("$.totalElements")
				.isEqualTo(2)
				.jsonPath("$.content[0].number")
				.isEqualTo(1)
				.jsonPath("$.content[0].saga.number")
				.isEqualTo(1)
				.jsonPath("$.content[0].saga.title")
				.isEqualTo("East Blue");
		}
		ok("/v1/en/arcs?saga=2").jsonPath("$.totalElements").isEqualTo(1).jsonPath("$.content[0].number").isEqualTo(3);
	}

	@Test
	void filtersTheChaptersByArcAndVolumeAndCombinesThem() {
		for (String arc : new String[] { this.arc1.toString(), "1" }) {
			ok("/v1/en/chapters?arc=" + arc).jsonPath("$.totalElements")
				.isEqualTo(3)
				.jsonPath("$.content[2].number")
				.isEqualTo(3)
				.jsonPath("$.content[0].arc.number")
				.isEqualTo(1)
				.jsonPath("$.content[0].arc.saga")
				.doesNotExist();
		}
		for (String volume : new String[] { this.volume2.toString(), "2" }) {
			ok("/v1/en/chapters?volume=" + volume).jsonPath("$.totalElements")
				.isEqualTo(1)
				.jsonPath("$.content[0].number")
				.isEqualTo(4)
				.jsonPath("$.content[0].volume.title")
				.isEqualTo("Volume Crew");
		}
		ok("/v1/en/chapters?arc=1&volume=1").jsonPath("$.totalElements")
			.isEqualTo(2)
			.jsonPath("$.content[1].number")
			.isEqualTo(2);
		ok("/v1/en/chapters?arc=2&volume=1").jsonPath("$.totalElements").isEqualTo(0);
	}

	@Test
	void filtersTheEpisodesByArc() {
		ok("/v1/en/episodes?arc=1").jsonPath("$.totalElements").isEqualTo(2);
		ok("/v1/en/episodes?arc=" + this.arc1).jsonPath("$.totalElements").isEqualTo(2);
		ok("/v1/en/episodes?arc=2").jsonPath("$.totalElements").isEqualTo(0);
	}

	@Test
	void anUnknownFilterValueListsNothing() {
		ok("/v1/en/arcs?saga=99").jsonPath("$.totalElements").isEqualTo(0).jsonPath("$.content.length()").isEqualTo(0);
		ok("/v1/en/arcs?saga=" + UUID.randomUUID()).jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/chapters?arc=99").jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/chapters?volume=99").jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/episodes?arc=99").jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/chapters?arc=").jsonPath("$.totalElements").isEqualTo(4);
	}

	@Test
	void servesASagaWithItsArcsAndWhatItsChaptersAndEpisodesAmountTo() {
		for (String idOrSlug : new String[] { "1", this.saga1.toString() }) {
			ok("/v1/en/sagas/" + idOrSlug).jsonPath("$.id")
				.isEqualTo(this.saga1.toString())
				.jsonPath("$.slug")
				.isEqualTo("1")
				.jsonPath("$.number")
				.isEqualTo(1)
				.jsonPath("$.title")
				.isEqualTo("East Blue")
				.jsonPath("$.description")
				.isEqualTo("Description of East Blue")
				.jsonPath("$.arcs.length()")
				.isEqualTo(2)
				.jsonPath("$.arcs[0].id")
				.isEqualTo(this.arc1.toString())
				.jsonPath("$.arcs[0].slug")
				.isEqualTo("1")
				.jsonPath("$.arcs[0].number")
				.isEqualTo(1)
				.jsonPath("$.arcs[0].title")
				.isEqualTo("Romance Dawn")
				.jsonPath("$.arcs[0].saga")
				.doesNotExist()
				.jsonPath("$.arcs[1].number")
				.isEqualTo(2)
				.jsonPath("$.chapters.count")
				.isEqualTo(4)
				.jsonPath("$.chapters.firstNumber")
				.isEqualTo(1)
				.jsonPath("$.chapters.lastNumber")
				.isEqualTo(4)
				.jsonPath("$.chapters.firstDate")
				.isEqualTo("1997-07-22")
				.jsonPath("$.chapters.lastDate")
				.isEqualTo("1997-09-01")
				.jsonPath("$.episodes.count")
				.isEqualTo(2)
				.jsonPath("$.episodes.firstNumber")
				.isEqualTo(1)
				.jsonPath("$.episodes.lastNumber")
				.isEqualTo(2)
				.jsonPath("$.episodes.firstDate")
				.isEqualTo("1999-10-20")
				.jsonPath("$.episodes.lastDate")
				.isEqualTo("1999-11-17");
		}
		ok("/v1/it/sagas/1").jsonPath("$.title")
			.isEqualTo("Blu Orientale")
			.jsonPath("$.arcs[0].title")
			.isEqualTo("Alba");
	}

	@Test
	void aSagaWithoutChaptersOrEpisodesAmountsToNothing() {
		ok("/v1/en/sagas/2").jsonPath("$.arcs.length()")
			.isEqualTo(1)
			.jsonPath("$.chapters.count")
			.isEqualTo(0)
			.jsonPath("$.chapters.firstNumber")
			.isEmpty()
			.jsonPath("$.chapters.lastNumber")
			.isEmpty()
			.jsonPath("$.chapters.firstDate")
			.isEmpty()
			.jsonPath("$.chapters.lastDate")
			.isEmpty()
			.jsonPath("$.episodes.count")
			.isEqualTo(0);
	}

	@Test
	void servesAnArcWithItsSagaAndSpans() {
		for (String idOrSlug : new String[] { "1", this.arc1.toString() }) {
			ok("/v1/en/arcs/" + idOrSlug).jsonPath("$.id")
				.isEqualTo(this.arc1.toString())
				.jsonPath("$.title")
				.isEqualTo("Romance Dawn")
				.jsonPath("$.saga.id")
				.isEqualTo(this.saga1.toString())
				.jsonPath("$.saga.slug")
				.isEqualTo("1")
				.jsonPath("$.saga.number")
				.isEqualTo(1)
				.jsonPath("$.saga.title")
				.isEqualTo("East Blue")
				.jsonPath("$.chapters.count")
				.isEqualTo(3)
				.jsonPath("$.chapters.firstNumber")
				.isEqualTo(1)
				.jsonPath("$.chapters.lastNumber")
				.isEqualTo(3)
				// Dates are the Japanese ones only: chapter 1 also came out in Italy in
				// 2001.
				.jsonPath("$.chapters.firstDate")
				.isEqualTo("1997-07-22")
				.jsonPath("$.chapters.lastDate")
				.isEqualTo("1997-08-11")
				.jsonPath("$.episodes.count")
				.isEqualTo(2)
				.jsonPath("$.episodes.firstDate")
				.isEqualTo("1999-10-20")
				.jsonPath("$.episodes.lastDate")
				.isEqualTo("1999-11-17");
		}
		ok("/v1/it/arcs/3").jsonPath("$.saga.title").isEqualTo("Alabasta IT");
	}

	@Test
	void servesAVolumeWithItsCoverReleasesChaptersAndArcs() {
		for (String idOrSlug : new String[] { "1", this.volume1.toString() }) {
			ok("/v1/en/volumes/" + idOrSlug).jsonPath("$.id")
				.isEqualTo(this.volume1.toString())
				.jsonPath("$.title")
				.isEqualTo("Volume Dawn")
				.jsonPath("$.japaneseTitle")
				.isEqualTo("ROMANCE DAWN")
				.jsonPath("$.cover")
				.isEqualTo("v1/images/" + this.cover)
				.jsonPath("$.releases.length()")
				.isEqualTo(3)
				.jsonPath("$.releases[0].country")
				.isEqualTo("JP")
				.jsonPath("$.releases[0].date")
				.isEqualTo("1997-12-24")
				.jsonPath("$.releases[1].country")
				.isEqualTo("IT")
				.jsonPath("$.releases[1].date")
				.isEqualTo("2002-05-01")
				.jsonPath("$.releases[2].country")
				.isEqualTo("US")
				.jsonPath("$.chapters.count")
				.isEqualTo(2)
				.jsonPath("$.chapters.firstNumber")
				.isEqualTo(1)
				.jsonPath("$.chapters.lastNumber")
				.isEqualTo(2)
				.jsonPath("$.chapters.firstDate")
				.isEqualTo("1997-07-22")
				.jsonPath("$.chapters.lastDate")
				.isEqualTo("1997-08-04")
				.jsonPath("$.arcs.length()")
				.isEqualTo(1)
				.jsonPath("$.arcs[0].id")
				.isEqualTo(this.arc1.toString())
				.jsonPath("$.arcs[0].title")
				.isEqualTo("Romance Dawn")
				.jsonPath("$.arcs[0].saga")
				.doesNotExist();
		}
		ok("/v1/en/volumes/2").jsonPath("$.cover")
			.isEmpty()
			.jsonPath("$.japaneseTitle")
			.isEmpty()
			.jsonPath("$.releases.length()")
			.isEqualTo(0)
			.jsonPath("$.arcs[0].number")
			.isEqualTo(2);
		ok("/v1/en/volumes").jsonPath("$.content[0].cover").isEqualTo("v1/images/" + this.cover);
	}

	@Test
	void servesAChapterWithItsArcVolumeAndReleases() {
		for (String idOrSlug : new String[] { "1", this.chapter1.toString() }) {
			ok("/v1/en/chapters/" + idOrSlug).jsonPath("$.id")
				.isEqualTo(this.chapter1.toString())
				.jsonPath("$.title")
				.isEqualTo("Dawn")
				.jsonPath("$.description")
				.isEqualTo("Description of Dawn")
				.jsonPath("$.japaneseTitle")
				.isEqualTo("ROMANCE DAWN")
				.jsonPath("$.pageCount")
				.isEqualTo(53)
				.jsonPath("$.arc.id")
				.isEqualTo(this.arc1.toString())
				.jsonPath("$.arc.title")
				.isEqualTo("Romance Dawn")
				.jsonPath("$.volume.id")
				.isEqualTo(this.volume1.toString())
				.jsonPath("$.volume.number")
				.isEqualTo(1)
				.jsonPath("$.volume.title")
				.isEqualTo("Volume Dawn")
				.jsonPath("$.volume.cover")
				.isEqualTo("v1/images/" + this.cover)
				.jsonPath("$.releases.length()")
				.isEqualTo(2)
				.jsonPath("$.releases[0].country")
				.isEqualTo("JP")
				.jsonPath("$.releases[0].date")
				.isEqualTo("1997-07-22")
				.jsonPath("$.releases[1].country")
				.isEqualTo("IT");
		}
		ok("/v1/it/chapters/3").jsonPath("$.id")
			.isEqualTo(this.chapter3.toString())
			.jsonPath("$.volume")
			.isEmpty()
			.jsonPath("$.japaneseTitle")
			.isEmpty()
			.jsonPath("$.pageCount")
			.isEmpty();
		ok("/v1/en/chapters/4").jsonPath("$.volume.cover").isEmpty();
	}

	@Test
	void servesAnEpisodeWithItsArcAndBroadcastsAndNoArcForAFiller() {
		for (String idOrSlug : new String[] { "1", this.episode1.toString() }) {
			ok("/v1/en/episodes/" + idOrSlug).jsonPath("$.title")
				.isEqualTo("Big Entrance")
				.jsonPath("$.japaneseTitle")
				.isEqualTo("ENTRANCE")
				.jsonPath("$.arc.id")
				.isEqualTo(this.arc1.toString())
				.jsonPath("$.arc.title")
				.isEqualTo("Romance Dawn")
				.jsonPath("$.broadcasts.length()")
				.isEqualTo(2)
				.jsonPath("$.broadcasts[0].country")
				.isEqualTo("JP")
				.jsonPath("$.broadcasts[0].date")
				.isEqualTo("1999-10-20")
				.jsonPath("$.broadcasts[1].country")
				.isEqualTo("US");
		}
		ok("/v1/en/episodes/3").jsonPath("$.id")
			.isEqualTo(this.episode3.toString())
			.jsonPath("$.arc")
			.isEmpty()
			.jsonPath("$.broadcasts.length()")
			.isEqualTo(0);
		ok("/v1/en/episodes").jsonPath("$.content[2].arc").isEmpty();
	}

	@Test
	void aDraftChapterIsInvisible() {
		get("/v1/en/chapters/99").expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("CONTENT_NOT_FOUND");
		ok("/v1/en/chapters").jsonPath("$.totalElements").isEqualTo(4);
		ok("/v1/en/chapters?q=99").jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/arcs/1").jsonPath("$.chapters.count").isEqualTo(3).jsonPath("$.chapters.lastNumber").isEqualTo(3);
	}

	@Test
	void aRetiredChapterIsInvisibleAndLeavesTheSpans() {
		this.fixture.retire(this.chapter3);

		get("/v1/en/chapters/3").expectStatus().isNotFound();
		get("/v1/en/chapters/" + this.chapter3).expectStatus().isNotFound();
		ok("/v1/en/chapters").jsonPath("$.totalElements").isEqualTo(3);
		ok("/v1/en/arcs/1").jsonPath("$.chapters.count")
			.isEqualTo(2)
			.jsonPath("$.chapters.lastNumber")
			.isEqualTo(2)
			.jsonPath("$.chapters.lastDate")
			.isEqualTo("1997-08-04");
	}

	@Test
	void anArcWithoutChaptersOnlineAmountsToNothing() {
		this.fixture.retire(this.chapter4);

		ok("/v1/en/arcs/2").jsonPath("$.chapters.count")
			.isEqualTo(0)
			.jsonPath("$.chapters.firstNumber")
			.isEmpty()
			.jsonPath("$.chapters.lastNumber")
			.isEmpty()
			.jsonPath("$.chapters.firstDate")
			.isEmpty()
			.jsonPath("$.chapters.lastDate")
			.isEmpty();
		ok("/v1/en/sagas/1").jsonPath("$.chapters.count").isEqualTo(3).jsonPath("$.chapters.lastNumber").isEqualTo(3);
		ok("/v1/en/volumes/2").jsonPath("$.chapters.count").isEqualTo(0).jsonPath("$.arcs.length()").isEqualTo(0);
	}

	@Test
	void answers404ForAnUnknownLanguageAndForUnknownNumbers() {
		for (String list : new String[] { "sagas", "arcs", "volumes", "chapters", "episodes" }) {
			get("/v1/xx/" + list).expectStatus()
				.isNotFound()
				.expectBody()
				.jsonPath("$.errorCode")
				.isEqualTo("LANGUAGE_NOT_AVAILABLE");
			get("/v1/en/" + list + "/999").expectStatus()
				.isNotFound()
				.expectBody()
				.jsonPath("$.errorCode")
				.isEqualTo("CONTENT_NOT_FOUND");
			get("/v1/en/" + list + "/" + UUID.randomUUID()).expectStatus().isNotFound();
		}
	}

	@Test
	void aTitleOnlyInItalianIsAbsentFromTheEnglishList() {
		this.fixture.publishChapter(5, this.arc1, null, null, null, it("Solo italiano"));

		ok("/v1/it/chapters").jsonPath("$.totalElements").isEqualTo(5);
		ok("/v1/en/chapters").jsonPath("$.totalElements").isEqualTo(4);
		get("/v1/en/chapters/5").expectStatus().isNotFound();
		ok("/v1/it/chapters/5").jsonPath("$.title").isEqualTo("Solo italiano");
	}

	@Test
	void servesTheCoverOfAnOnlineVolumeAsAPngOnlyWhileItIsOnline() {
		byte[] body = get("/v1/images/" + this.cover).expectStatus()
			.isOk()
			.expectHeader()
			.contentType("image/webp")
			.expectBody(byte[].class)
			.returnResult()
			.getResponseBody();
		assertThat(body).isEqualTo(WEBP_BYTES);

		this.fixture.retire(this.volume1);

		this.client.get()
			.uri("/v1/images/" + this.cover)
			.accept(MediaType.parseMediaType("image/webp"), MediaType.ALL)
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.NOT_FOUND)
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("CONTENT_NOT_FOUND");
	}

	private static Translation it(String title) {
		return Translation.of("it", title);
	}

	private static Translation en(String title) {
		return Translation.of("en", title);
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return get(uri).expectStatus().isOk().expectBody();
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
