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

import java.util.UUID;

/**
 * Long texts at N, end to end (workflow 3.7.3, 3.8; reveals plan R7b): a paragraph is
 * read from its discovery until its end, then superseded. Seeded for every test: chapters
 * 1, 5, 8 and episode 1, everything met at chapter 1. Sabo: his childhood (chapter 1,
 * episode 1), believed dead from chapter 1 to chapter 8, alive from chapter 8. The Mera
 * Mera: its advantage from chapter 5. Saga 1: its description from chapter 5.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiReaderAxisParagraphsIntegrationTest extends PublishedDatabaseTest {

	private static final String CHILDHOOD = "Childhood with Ace and Luffy.";

	private static final String BELIEVED_DEAD = "Ace and Luffy believe him dead.";

	private static final String SURVIVED = "He survived, saved by Dragon.";

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID saga = this.fixture.publishSaga(1, Translation.of("en", "East Blue"));
		UUID arc = this.fixture.publishArc(1, saga, Translation.of("en", "Romance Dawn"));
		UUID chapter1 = this.fixture.publishChapter(1, arc, null, null, null, Translation.of("en", "Dawn"));
		UUID chapter5 = this.fixture.publishChapter(5, arc, null, null, null, Translation.of("en", "Flame"));
		UUID chapter8 = this.fixture.publishChapter(8, arc, null, null, null, Translation.of("en", "Brothers"));
		UUID episode1 = this.fixture.publishEpisode(1, arc, null, Translation.of("en", "Big Entrance"));
		this.fixture.paragraphWindow(saga, 0, Window.builder().discoveredChapter(chapter5).build());

		UUID sabo = this.fixture.publishCharacter(Persona.builder().romaji("Sabo").build(),
				new Translation("en", "Sabo", CHILDHOOD, null, null));
		this.fixture.firstAppearance(sabo, chapter1);
		this.fixture.firstAppearanceEpisode(sabo, episode1);
		this.fixture.paragraphWindow(sabo, 0,
				Window.builder().discoveredChapter(chapter1).discoveredEpisode(episode1).build());
		this.fixture.windowedParagraph(sabo, "DESCRIPTION", "en", BELIEVED_DEAD,
				Window.builder()
					.discoveredChapter(chapter1)
					.discoveredEpisode(episode1)
					.endedChapter(chapter8)
					.build());
		this.fixture.windowedParagraph(sabo, "DESCRIPTION", "en", SURVIVED,
				Window.builder().discoveredChapter(chapter8).build());

		UUID paramecia = this.fixture.publish("Paramecia", Translation.of("en", "Paramecia"));
		this.fixture.firstAppearance(paramecia, chapter1);
		UUID mera = this.fixture.publishFruit("Mera Mera no Mi", paramecia, null,
				new Translation("en", "Flame-Flame", "A logia of fire.", "Shoots fire.", null));
		this.fixture.firstAppearance(mera, chapter1);
		this.fixture.paragraphWindow(mera, 0, Window.builder().discoveredChapter(chapter1).build());
		this.fixture.paragraphWindow(mera, 1, Window.builder().discoveredChapter(chapter5).build());
	}

	@Test
	void readsABeliefUntilItsEndThenTheParagraphThatSupersedesIt() {
		ok("/v1/en/characters/sabo?asOfChapter=1").jsonPath("$.description.length()")
			.isEqualTo(2)
			.jsonPath("$.description[1].text")
			.isEqualTo(BELIEVED_DEAD);
		ok("/v1/en/characters/sabo?asOfChapter=8").jsonPath("$.description.length()")
			.isEqualTo(2)
			.jsonPath("$.description[0].text")
			.isEqualTo(CHILDHOOD)
			.jsonPath("$.description[1].text")
			.isEqualTo(SURVIVED);
		ok("/v1/en/characters/sabo").jsonPath("$.description.length()")
			.isEqualTo(2)
			.jsonPath("$.description[1].text")
			.isEqualTo(SURVIVED)
			.jsonPath("$.description[0].discovered.episode")
			.isEqualTo(1);
	}

	@Test
	void readsOnlyThePartsOnTheReadersAxis() {
		ok("/v1/en/characters/sabo?asOfEpisode=1").jsonPath("$.description.length()")
			.isEqualTo(2)
			.jsonPath("$.description[1].text")
			.isEqualTo(BELIEVED_DEAD)
			.jsonPath("$.description[0].discovered.chapter")
			.isEmpty()
			.jsonPath("$.description[0].discovered.episode")
			.isEqualTo(1);
		ok("/v1/en/characters/sabo?asOfChapter=1").jsonPath("$.description[0].discovered.episode").isEmpty();
	}

	@Test
	void showsEverySectionFromItsDiscovery() {
		ok("/v1/en/devil-fruits/mera-mera-no-mi?asOfChapter=1").jsonPath("$.description.length()")
			.isEqualTo(1)
			.jsonPath("$.advantages.length()")
			.isEqualTo(0);
		ok("/v1/en/devil-fruits/mera-mera-no-mi?asOfChapter=5").jsonPath("$.advantages[0].text")
			.isEqualTo("Shoots fire.");
		ok("/v1/en/sagas/1?asOfChapter=1").jsonPath("$.description.length()").isEqualTo(0);
		ok("/v1/en/sagas/1?asOfChapter=5").jsonPath("$.description.length()").isEqualTo(1);
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return this.client.get().uri(uri).exchange().expectStatus().isOk().expectBody();
	}

}
