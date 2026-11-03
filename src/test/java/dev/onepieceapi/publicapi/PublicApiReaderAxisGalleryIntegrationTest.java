package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
import dev.onepieceapi.publicapi.PublishedContentFixture.Window;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The gallery and the main portrait at N, end to end (workflow 3.5.1, 3.8; reveals plan
 * R7b): only the images discovered at N on the reader's axis, former from their end, the
 * portrait derived at N in the detail and in the rows. Seeded: chapters 1, 5, 8 and
 * episodes 1, 4; Luffy met at chapter 1 and episode 1, with a gallery: East Blue
 * (appearance, chapter 1 and episode 1, ended at chapter 5), Gear 5 (form, chapter 8),
 * after the timeskip (appearance, chapter 5), anime only (appearance, episode 4).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiReaderAxisGalleryIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private String eastBlue;

	private String gear5;

	private String timeskip;

	private String animeOnly;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID arc = this.fixture.publishArc(1, this.fixture.publishSaga(1, en("East Blue")), en("Romance Dawn"));
		UUID chapter1 = this.fixture.publishChapter(1, arc, null, null, null, en("Dawn"));
		UUID chapter5 = this.fixture.publishChapter(5, arc, null, null, null, en("Return"));
		UUID chapter8 = this.fixture.publishChapter(8, arc, null, null, null, en("Nika"));
		UUID episode1 = this.fixture.publishEpisode(1, arc, null, en("Big Entrance"));
		UUID episode4 = this.fixture.publishEpisode(4, arc, null, en("Filler"));

		UUID luffy = this.fixture.publishCharacter(Persona.builder().romaji("Monkey D. Luffy").build(),
				en("Monkey D. Luffy"));
		this.fixture.firstAppearance(luffy, chapter1);
		this.fixture.firstAppearanceEpisode(luffy, episode1);
		this.eastBlue = this.fixture.image('a', new byte[] { 1 });
		this.gear5 = this.fixture.image('b', new byte[] { 2 });
		this.timeskip = this.fixture.image('c', new byte[] { 3 });
		this.animeOnly = this.fixture.image('d', new byte[] { 4 });
		this.fixture.galleryImage(luffy, this.eastBlue, "ASPETTO", null, false, Map.of("en", "East Blue"));
		this.fixture.galleryImage(luffy, this.gear5, "FORMA", null, false, Map.of("en", "Gear 5"));
		this.fixture.galleryImage(luffy, this.timeskip, "ASPETTO", null, false, Map.of("en", "After the timeskip"));
		this.fixture.galleryImage(luffy, this.animeOnly, "ASPETTO", null, false, Map.of("en", "Anime only"));
		this.fixture.galleryWindow(luffy, 0,
				Window.builder()
					.discoveredChapter(chapter1)
					.discoveredEpisode(episode1)
					.endedChapter(chapter5)
					.build());
		this.fixture.galleryWindow(luffy, 1, Window.builder().discoveredChapter(chapter8).build());
		this.fixture.galleryWindow(luffy, 2, Window.builder().discoveredChapter(chapter5).build());
		this.fixture.galleryWindow(luffy, 3, Window.builder().discoveredEpisode(episode4).build());
	}

	@Test
	void showsOnlyTheImagesDiscoveredAndAnEndAfterNotYet() {
		ok("/v1/en/characters/monkey-d-luffy?asOfChapter=1").jsonPath("$.gallery[*].image")
			.isEqualTo(List.of(path(this.eastBlue)))
			.jsonPath("$.gallery[0].former")
			.isEqualTo(false)
			.jsonPath("$.gallery[0].ended")
			.isEmpty()
			.jsonPath("$.gallery[0].caption")
			.isEqualTo("East Blue");
		ok("/v1/en/characters/monkey-d-luffy?asOfChapter=5").jsonPath("$.gallery[*].image")
			.isEqualTo(List.of(path(this.eastBlue), path(this.timeskip)))
			.jsonPath("$.gallery[0].former")
			.isEqualTo(true)
			.jsonPath("$.gallery[0].ended.chapter")
			.isEqualTo(5);
		ok("/v1/en/characters/monkey-d-luffy?asOfChapter=8").jsonPath("$.gallery[*].image")
			.isEqualTo(List.of(path(this.eastBlue), path(this.gear5), path(this.timeskip)));
	}

	@Test
	void readsOnlyTheImagesAndPointsOnTheReadersAxis() {
		ok("/v1/en/characters/monkey-d-luffy?asOfEpisode=1").jsonPath("$.gallery[*].image")
			.isEqualTo(List.of(path(this.eastBlue)))
			.jsonPath("$.gallery[0].former")
			.isEqualTo(false)
			.jsonPath("$.gallery[0].discovered.chapter")
			.isEmpty()
			.jsonPath("$.gallery[0].discovered.episode")
			.isEqualTo(1)
			.jsonPath("$.gallery[0].ended")
			.isEmpty();
		ok("/v1/en/characters/monkey-d-luffy?asOfEpisode=4").jsonPath("$.gallery[*].image")
			.isEqualTo(List.of(path(this.eastBlue), path(this.animeOnly)));
	}

	@Test
	void derivesTheMainPortraitAtNInTheDetail() {
		ok("/v1/en/characters/monkey-d-luffy?asOfChapter=1").jsonPath("$.image").isEqualTo(path(this.eastBlue));
		ok("/v1/en/characters/monkey-d-luffy?asOfChapter=8").jsonPath("$.image").isEqualTo(path(this.timeskip));
		ok("/v1/en/characters/monkey-d-luffy?asOfEpisode=1").jsonPath("$.image").isEqualTo(path(this.eastBlue));
		ok("/v1/en/characters/monkey-d-luffy?asOfEpisode=4").jsonPath("$.image").isEqualTo(path(this.animeOnly));
		ok("/v1/en/characters/monkey-d-luffy").jsonPath("$.image").isEqualTo(path(this.animeOnly));
	}

	@Test
	void derivesTheMainPortraitAtNInTheRows() {
		ok("/v1/en/characters?asOfChapter=1").jsonPath("$.content[0].image").isEqualTo(path(this.eastBlue));
		ok("/v1/en/characters?asOfChapter=5").jsonPath("$.content[0].image").isEqualTo(path(this.timeskip));
		ok("/v1/en/characters?asOfEpisode=4").jsonPath("$.content[0].image").isEqualTo(path(this.animeOnly));
		ok("/v1/en/characters").jsonPath("$.content[0].image").isEqualTo(path(this.animeOnly));
	}

	private static String path(String imageId) {
		return "v1/images/" + imageId;
	}

	private static Translation en(String name) {
		return Translation.of("en", name);
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return this.client.get().uri(uri).exchange().expectStatus().isOk().expectBody();
	}

}
