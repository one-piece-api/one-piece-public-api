package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.LocalDate;
import java.util.UUID;

/**
 * The spoiler filter {@code asOfChapter} on the story structure end to end (plan of the
 * timeline, T8, TL5). Seeded for every test, in English: saga 1 with arc 1 (chapters 1
 * and 2, volume 1, episode 1) and arc 2 (chapter 10, volume 2, episode 2); a filler
 * episode 3 with no arc. Chapters 1 and 2 came out in Japan on 1997-07-22 and 1997-08-04.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiStorySpoilerFilterIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID arc2;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID saga = this.fixture.publishSaga(1, Translation.of("en", "East Blue"));
		UUID arc1 = this.fixture.publishArc(1, saga, Translation.of("en", "Romance Dawn"));
		this.arc2 = this.fixture.publishArc(2, saga, Translation.of("en", "Orange Town"));
		UUID volume1 = this.fixture.publishVolume(1, null, null, Translation.of("en", "Romance Dawn"));
		UUID volume2 = this.fixture.publishVolume(2, null, null, Translation.of("en", "Buggy"));
		UUID chapter1 = this.fixture.publishChapter(1, arc1, volume1, null, null, Translation.of("en", "Dawn"));
		UUID chapter2 = this.fixture.publishChapter(2, arc1, volume1, null, null, Translation.of("en", "Pirate"));
		this.fixture.publishChapter(10, this.arc2, volume2, null, null, Translation.of("en", "Incident"));
		this.fixture.chapterRelease(chapter1, "JP", LocalDate.of(1997, 7, 22));
		this.fixture.chapterRelease(chapter2, "JP", LocalDate.of(1997, 8, 4));
		this.fixture.publishEpisode(1, arc1, null, Translation.of("en", "I'm Luffy"));
		this.fixture.publishEpisode(2, this.arc2, null, Translation.of("en", "The Great Swordsman"));
		this.fixture.publishEpisode(3, null, null, Translation.of("en", "Filler"));
	}

	@Test
	void withoutTheFilterEverythingOnlineIsShown() {
		total("/v1/en/chapters", 3);
		total("/v1/en/arcs", 2);
		total("/v1/en/episodes", 3);
		get("/v1/en/arcs/2").expectStatus().isOk().expectBody().jsonPath("$.chapters.count").isEqualTo(1);
	}

	@Test
	void listsOnlyWhatHasBegunByThatChapterInclusive() {
		total("/v1/en/sagas?asOfChapter=0", 0);
		total("/v1/en/sagas?asOfChapter=1", 1);
		total("/v1/en/arcs?asOfChapter=9", 1);
		total("/v1/en/arcs?asOfChapter=10", 2);
		total("/v1/en/volumes?asOfChapter=2", 1);
		total("/v1/en/chapters?asOfChapter=2", 2);
		total("/v1/en/chapters?arc=" + this.arc2 + "&asOfChapter=9", 0);
	}

	@Test
	void showsTheEpisodesOfTheArcsBegunAndHidesTheFillersWithNoArc() {
		get("/v1/en/episodes?asOfChapter=9").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(1)
			.jsonPath("$.content[0].number")
			.isEqualTo(1);
		total("/v1/en/episodes?asOfChapter=5000", 2);
	}

	@Test
	void aDetailNotBegunYetAnswers404() {
		get("/v1/en/arcs/orange-town?asOfChapter=9").expectStatus().isNotFound();
		get("/v1/en/arcs/" + this.arc2 + "?asOfChapter=9").expectStatus().isNotFound();
		get("/v1/en/chapters/10?asOfChapter=9").expectStatus().isNotFound();
		get("/v1/en/volumes/2?asOfChapter=9").expectStatus().isNotFound();
		get("/v1/en/episodes/2?asOfChapter=9").expectStatus().isNotFound();
		get("/v1/en/chapters/10?asOfChapter=10").expectStatus().isOk();
	}

	@Test
	void cutsTheChaptersOfASagaArcAndVolumeAtTheReadersChapter() {
		get("/v1/en/arcs/1?asOfChapter=1").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.chapters.count")
			.isEqualTo(1)
			.jsonPath("$.chapters.firstNumber")
			.isEqualTo(1)
			.jsonPath("$.chapters.lastNumber")
			.isEqualTo(1)
			.jsonPath("$.chapters.firstDate")
			.isEqualTo("1997-07-22")
			.jsonPath("$.chapters.lastDate")
			.isEqualTo("1997-07-22");
		get("/v1/en/sagas/1?asOfChapter=2").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.chapters.count")
			.isEqualTo(2)
			.jsonPath("$.chapters.lastNumber")
			.isEqualTo(2)
			.jsonPath("$.arcs.length()")
			.isEqualTo(1);
		get("/v1/en/volumes/1?asOfChapter=1").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.chapters.count")
			.isEqualTo(1);
		get("/v1/en/sagas/1?asOfChapter=5000").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.chapters.count")
			.isEqualTo(3)
			.jsonPath("$.chapters.lastNumber")
			.isEqualTo(10);
	}

	private void total(String uri, int expected) {
		get(uri).expectStatus().isOk().expectBody().jsonPath("$.totalElements").isEqualTo(expected);
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
