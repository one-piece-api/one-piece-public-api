package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Belonging;
import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
import dev.onepieceapi.publicapi.PublishedContentFixture.Poster;
import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
import dev.onepieceapi.publicapi.PublishedContentFixture.Window;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The spoiler filter by first appearance, end to end (workflow 3.8; reveals plan R7a):
 * real views, real HTTP. Seeded for every test: saga 1 with arc 1 (chapters 1 and 2,
 * episode 1) and arc 2 (chapter 5, episode 4); volume 1 (chapters 1, 2) and volume 2
 * (chapter 5). Paramecia is met at chapter 1 and episode 1, Zoan at chapter 5 only; the
 * Gomu Gomu at chapter 1 and episode 1, the Hito Hito (a Zoan) at chapter 2, the Ushi
 * Ushi (a Zoan) at chapter 5. Luffy is met at chapter 1 and episode 1, Chopper at chapter
 * 5, both members of the Straw Hats (met at chapter 1); Chopper's bounty is met at
 * chapter 1.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiReaderAxisIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID saga;

	private UUID ushi;

	private UUID bounty;

	private UUID strawHats;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		this.saga = this.fixture.publishSaga(1, en("East Blue"));
		UUID arc1 = this.fixture.publishArc(1, this.saga, en("Romance Dawn"));
		UUID arc2 = this.fixture.publishArc(2, this.saga, en("Drum Island"));
		UUID volume1 = this.fixture.publishVolume(1, null, null, en("Volume One"));
		UUID volume2 = this.fixture.publishVolume(2, null, null, en("Volume Two"));
		UUID chapter1 = this.fixture.publishChapter(1, arc1, volume1, null, null, en("Dawn"));
		UUID chapter2 = this.fixture.publishChapter(2, arc1, volume1, null, null, en("Straw Hat"));
		UUID chapter5 = this.fixture.publishChapter(5, arc2, volume2, null, null, en("Reindeer"));
		UUID episode1 = this.fixture.publishEpisode(1, arc1, null, en("Big Entrance"));
		this.fixture.publishEpisode(4, arc2, null, en("Snow"));
		this.fixture.chapterRelease(chapter1, "JP", LocalDate.of(1997, 7, 22));
		this.fixture.chapterRelease(chapter2, "JP", LocalDate.of(1997, 8, 4));
		this.fixture.chapterRelease(chapter5, "JP", LocalDate.of(1997, 9, 1));

		UUID paramecia = this.fixture.publish("Paramecia", en("Paramecia"));
		met(paramecia, chapter1, episode1);
		UUID zoan = this.fixture.publish("Zoan", en("Zoan"));
		met(zoan, chapter5, null);
		met(this.fixture.publishFruit("Gomu Gomu no Mi", paramecia, null, en("Gum-Gum Fruit")), chapter1, episode1);
		met(this.fixture.publishFruit("Hito Hito no Mi", zoan, null, en("Human-Human Fruit")), chapter2, null);
		this.ushi = this.fixture.publishFruit("Ushi Ushi no Mi", zoan, null, en("Ox-Ox Fruit"));
		met(this.ushi, chapter5, null);

		this.strawHats = this.fixture.publishAffiliation("Mugiwara no Ichimi", null, en("Straw Hat Pirates"));
		met(this.strawHats, chapter1, null);
		var crew = List.of(new Belonging(this.strawHats, Map.of(), null, false));
		UUID luffy = this.fixture.publishCharacter(
				Persona.builder().romaji("Monkey D. Luffy").affiliations(crew).build(), en("Monkey D. Luffy"));
		met(luffy, chapter1, episode1);
		this.fixture.periodWindow(luffy, 0, 0, Window.builder().discoveredChapter(chapter1).build());
		UUID chopper = this.fixture.publishCharacter(
				Persona.builder().romaji("Tony Tony Chopper").affiliations(crew).build(), en("Tony Tony Chopper"));
		met(chopper, chapter5, null);
		this.fixture.periodWindow(chopper, 0, 0, Window.builder().discoveredChapter(chapter5).build());
		this.bounty = this.fixture.bounty(
				Poster.builder().posterName("Tony Tony Chopper").amount(50).holderId(chopper).build(),
				en("Chopper's poster"));
		met(this.bounty, chapter1, null);
	}

	@Test
	void refusesBothAxesAndANumberBelowOne() {
		for (String uri : new String[] { "/v1/en/devil-fruits?asOfChapter=1&asOfEpisode=1",
				"/v1/en/devil-fruits/" + this.ushi + "?asOfChapter=1&asOfEpisode=1", "/v1/en/sagas?asOfChapter=0",
				"/v1/en/chapters?asOfEpisode=-1" }) {
			get(uri).expectStatus().isBadRequest().expectBody().jsonPath("$.errorCode").isEqualTo("VALIDATION_FAILED");
		}
	}

	@Test
	void leavesOutOfListsAndCountsWhatTheReaderHasNotMet() {
		ok("/v1/en/devil-fruits").jsonPath("$.totalElements").isEqualTo(3);
		ok("/v1/en/devil-fruits?asOfChapter=1").jsonPath("$.totalElements")
			.isEqualTo(1)
			.jsonPath("$.content[0].name")
			.isEqualTo("Gum-Gum Fruit");
		ok("/v1/en/devil-fruits?asOfChapter=5&sort=name&size=1&page=2").jsonPath("$.totalElements")
			.isEqualTo(3)
			.jsonPath("$.content[0].name")
			.isEqualTo("Ox-Ox Fruit");
		ok("/v1/en/devil-fruits?asOfEpisode=4").jsonPath("$.totalElements").isEqualTo(1);
		ok("/v1/en/devil-fruits?asOfChapter=1&q=ox").jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/characters?asOfChapter=2").jsonPath("$.totalElements")
			.isEqualTo(1)
			.jsonPath("$.content[0].name")
			.isEqualTo("Monkey D. Luffy");
	}

	@Test
	void answersNotFoundForAContentNotMetYetByIdOrSlug() {
		for (String address : new String[] { this.ushi.toString(), "ushi-ushi-no-mi" }) {
			get("/v1/en/devil-fruits/" + address + "?asOfChapter=4").expectStatus()
				.isNotFound()
				.expectBody()
				.jsonPath("$.errorCode")
				.isEqualTo("CONTENT_NOT_FOUND");
			ok("/v1/en/devil-fruits/" + address + "?asOfChapter=5").jsonPath("$.name").isEqualTo("Ox-Ox Fruit");
		}
		get("/v1/en/devil-fruits/ushi-ushi-no-mi?asOfEpisode=10").expectStatus().isNotFound();
	}

	@Test
	void showsALinkOrAnEntryOnlyOnceTheReaderHasMetItsContent() {
		ok("/v1/en/devil-fruits/hito-hito-no-mi?asOfChapter=2").jsonPath("$.type").isEmpty();
		ok("/v1/en/devil-fruits/hito-hito-no-mi?asOfChapter=5").jsonPath("$.type.name").isEqualTo("Zoan");
		ok("/v1/en/devil-fruits?asOfChapter=2&sort=name").jsonPath("$.content[1].type").isEmpty();
		ok("/v1/en/devil-fruit-types/zoan?asOfChapter=5").jsonPath("$.devilFruits.length()").isEqualTo(2);
		ok("/v1/en/bounties/" + this.bounty + "?asOfChapter=1").jsonPath("$.holder").isEmpty();
		ok("/v1/en/bounties?asOfChapter=1").jsonPath("$.content[0].holder").isEmpty();
		ok("/v1/en/bounties?asOfChapter=5").jsonPath("$.content[0].holder.name").isEqualTo("Tony Tony Chopper");
		ok("/v1/en/affiliations/" + this.strawHats).jsonPath("$.memberCount").isEqualTo(2);
		ok("/v1/en/affiliations/" + this.strawHats + "?asOfChapter=2").jsonPath("$.memberCount").isEqualTo(1);
		ok("/v1/en/characters/monkey-d-luffy?asOfEpisode=1").jsonPath("$.affiliations.length()").isEqualTo(0);
	}

	@Test
	void showsTheFirstAppearanceOnlyOnTheReadersAxis() {
		ok("/v1/en/devil-fruits/gomu-gomu-no-mi").jsonPath("$.firstAppearance.chapter.number")
			.isEqualTo(1)
			.jsonPath("$.firstAppearance.episode.number")
			.isEqualTo(1);
		ok("/v1/en/devil-fruits/gomu-gomu-no-mi?asOfChapter=1").jsonPath("$.firstAppearance.chapter.number")
			.isEqualTo(1)
			.jsonPath("$.firstAppearance.episode")
			.isEmpty();
		ok("/v1/en/devil-fruits/gomu-gomu-no-mi?asOfEpisode=1").jsonPath("$.firstAppearance.chapter")
			.isEmpty()
			.jsonPath("$.firstAppearance.episode.number")
			.isEqualTo(1);
	}

	@Test
	void hidesTheOtherMediumAndCountsTheStoryUpToTheReader() {
		ok("/v1/en/episodes?asOfChapter=5").jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/chapters?asOfEpisode=4").jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/volumes?asOfEpisode=4").jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/chapters?asOfChapter=2").jsonPath("$.totalElements").isEqualTo(2);
		ok("/v1/en/volumes?asOfChapter=2").jsonPath("$.totalElements").isEqualTo(1);
		ok("/v1/en/arcs?asOfChapter=2").jsonPath("$.totalElements").isEqualTo(1);
		ok("/v1/en/arcs?asOfEpisode=4").jsonPath("$.totalElements").isEqualTo(2);
		ok("/v1/en/sagas/1?asOfChapter=2").jsonPath("$.chapters.count")
			.isEqualTo(2)
			.jsonPath("$.chapters.lastNumber")
			.isEqualTo(2)
			.jsonPath("$.chapters.lastDate")
			.isEqualTo("1997-08-04")
			.jsonPath("$.episodes.count")
			.isEqualTo(0)
			.jsonPath("$.arcs.length()")
			.isEqualTo(1);
		ok("/v1/en/sagas/1").jsonPath("$.chapters.count").isEqualTo(3).jsonPath("$.episodes.count").isEqualTo(2);
		get("/v1/en/chapters/5?asOfChapter=4").expectStatus().isNotFound();
	}

	@Test
	void answers304ToACurrentETagOnAFilteredAddress() {
		String uri = "/v1/en/devil-fruits?asOfChapter=1";
		String eTag = get(uri).expectStatus().isOk().returnResult(String.class).getResponseHeaders().getETag();

		this.client.get()
			.uri(uri)
			.header(HttpHeaders.IF_NONE_MATCH, eTag)
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.NOT_MODIFIED);
	}

	private void met(UUID contentId, UUID chapterId, UUID episodeId) {
		if (chapterId != null) {
			this.fixture.firstAppearance(contentId, chapterId);
		}
		if (episodeId != null) {
			this.fixture.firstAppearanceEpisode(contentId, episodeId);
		}
	}

	private static Translation en(String name) {
		return Translation.of("en", name);
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return get(uri).expectStatus().isOk().expectBody();
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
