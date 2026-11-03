package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.CharacterList;
import dev.onepieceapi.publicapi.PublishedContentFixture.Having;
import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
import dev.onepieceapi.publicapi.PublishedContentFixture.Poster;
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
 * The reader axis in the {@code v1} shape, without a filter (workflow 3.5.1, 3.7; reveals
 * plan R7 step 3): windows, gallery and main portrait, paragraphs, field reveals. Seeded:
 * chapters 1 and 2; Luffy with a gallery (a past appearance, a form, a current
 * appearance) and a superseded paragraph; Ace, believed alive until chapter 2; the Gomu
 * Gomu, revealed at chapter 2 to be the Hito Hito, Model: Nika, a Zoan; a bounty learned
 * inactive at chapter 2.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiReaderAxisContractIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID chapter2;

	private String childhood;

	private String portrait;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID arc = this.fixture.publishArc(1, this.fixture.publishSaga(1, en("East Blue")), en("Romance Dawn"));
		UUID chapter1 = this.fixture.publishChapter(1, arc, null, null, null, en("Romance Dawn"));
		this.chapter2 = this.fixture.publishChapter(2, arc, null, null, null, en("Straw Hat"));

		UUID paramecia = this.fixture.publish("Paramecia", en("Paramecia"));
		UUID zoan = this.fixture.publish("Zoan", en("Zoan"));
		UUID gomu = this.fixture.publishFruit("Hito Hito no Mi, Model: Nika", zoan, null,
				en("Human-Human Fruit, Model: Nika"));
		this.fixture.devilFruitInitialKnowledge(gomu, "Gomu Gomu no Mi", paramecia, Map.of("en", "Gum-Gum Fruit"));
		this.fixture.devilFruitReveal(gomu, this.chapter2, "Awakening", "NAME", "ROMAJI", "TYPE");

		UUID luffy = this.fixture.publishCharacter(
				Persona.builder().romaji("Monkey D. Luffy").devilFruits(List.of(new Having(gomu, false))).build(),
				en("Monkey D. Luffy"));
		this.fixture.firstAppearance(gomu, chapter1);
		this.fixture.firstAppearance(luffy, chapter1);
		this.fixture.window(luffy, CharacterList.DEVIL_FRUIT, 0, Window.builder().discoveredChapter(chapter1).build());
		this.childhood = this.fixture.image('a', new byte[] { 1 });
		String gear5 = this.fixture.image('b', new byte[] { 2 });
		this.portrait = this.fixture.image('c', new byte[] { 3 });
		this.fixture.galleryImage(luffy, this.childhood, "ASPETTO", chapter1, true, Map.of("en", "As a child"));
		this.fixture.galleryImage(luffy, gear5, "FORMA", this.chapter2, false, Map.of());
		this.fixture.galleryImage(luffy, this.portrait, "ASPETTO", this.chapter2, false, Map.of());
		this.fixture.formerParagraph(luffy, "en", "A rubber man.");

		UUID ace = this.fixture.publishCharacter(Persona.builder().romaji("Portgas D. Ace").status("DECEASED").build(),
				en("Portgas D. Ace"));
		this.fixture.characterInitialKnowledge(ace, "ALIVE");
		this.fixture.characterReveal(ace, this.chapter2, "Marineford", "STATUS");

		UUID bounty = this.fixture.bounty(
				Poster.builder().posterName("Portgas D. Ace").amount(550).inactive(true).build(), en("Ace's poster"));
		this.fixture.bountyInitialKnowledge(bounty, false);
		this.fixture.bountyReveal(bounty, this.chapter2, "Marineford", "INACTIVE");
	}

	@Test
	void showsTheGalleryInItsWindowsAndTheLastCurrentAppearanceAsThePortrait() {
		ok("/v1/en/characters/monkey-d-luffy").jsonPath("$.image")
			.isEqualTo("v1/images/" + this.portrait)
			.jsonPath("$.gallery[*].kind")
			.isEqualTo(List.of("APPEARANCE", "FORM", "APPEARANCE"))
			.jsonPath("$.gallery[0].image")
			.isEqualTo("v1/images/" + this.childhood)
			.jsonPath("$.gallery[0].caption")
			.isEqualTo("As a child")
			.jsonPath("$.gallery[0].former")
			.isEqualTo(true)
			.jsonPath("$.gallery[0].discovered.chapter")
			.isEqualTo(1)
			.jsonPath("$.gallery[0].discovered.episode")
			.isEmpty()
			.jsonPath("$.gallery[0].ended.chapter")
			.isEqualTo(PublishedContentFixture.ENDING_CHAPTER)
			.jsonPath("$.gallery[1].caption")
			.isEmpty()
			.jsonPath("$.gallery[2].former")
			.isEqualTo(false)
			.jsonPath("$.gallery[2].ended")
			.isEmpty();
		ok("/v1/en/characters?q=luffy").jsonPath("$.content[0].image").isEqualTo("v1/images/" + this.portrait);
	}

	@Test
	void showsOnlyTheParagraphsThatStillHold() {
		ok("/v1/en/characters/monkey-d-luffy").jsonPath("$.description[*].text")
			.isEqualTo(List.of("Description of Monkey D. Luffy"));
	}

	@Test
	void showsTheWindowOfADevilFruitOnBothSides() {
		ok("/v1/en/characters/monkey-d-luffy").jsonPath("$.devilFruits[0].devilFruit.slug")
			.isEqualTo("hito-hito-no-mi-model-nika")
			.jsonPath("$.devilFruits[0].former")
			.isEqualTo(false)
			.jsonPath("$.devilFruits[0].ended")
			.isEmpty();
		ok("/v1/en/devil-fruits/hito-hito-no-mi-model-nika").jsonPath("$.users[0].former")
			.isEqualTo(false)
			.jsonPath("$.users[0].ended")
			.isEmpty();
	}

	@Test
	void aRevealWithoutAValueOfItsOwnRevealsTheCurrentOne() {
		ok("/v1/en/characters/portgas-d-ace").jsonPath("$.status")
			.isEqualTo("DECEASED")
			.jsonPath("$.initialKnowledge.status")
			.isEqualTo("ALIVE")
			.jsonPath("$.initialKnowledge.name")
			.isEmpty()
			.jsonPath("$.reveals.length()")
			.isEqualTo(1)
			.jsonPath("$.reveals[0].title")
			.isEqualTo("Marineford")
			.jsonPath("$.reveals[0].revealed.chapter")
			.isEqualTo(2)
			.jsonPath("$.reveals[0].fields")
			.isEqualTo(List.of("STATUS"))
			.jsonPath("$.reveals[0].status")
			.isEqualTo("DECEASED")
			.jsonPath("$.reveals[0].name")
			.isEmpty();
		ok("/v1/en/devil-fruits/hito-hito-no-mi-model-nika").jsonPath("$.initialKnowledge.name")
			.isEqualTo("Gum-Gum Fruit")
			.jsonPath("$.initialKnowledge.slug")
			.isEqualTo("gomu-gomu-no-mi")
			.jsonPath("$.initialKnowledge.type.slug")
			.isEqualTo("paramecia")
			.jsonPath("$.reveals[0].fields")
			.isEqualTo(List.of("NAME", "ROMAJI", "TYPE"))
			.jsonPath("$.reveals[0].name")
			.isEqualTo("Human-Human Fruit, Model: Nika")
			.jsonPath("$.reveals[0].slug")
			.isEqualTo("hito-hito-no-mi-model-nika")
			.jsonPath("$.reveals[0].type.slug")
			.isEqualTo("zoan");
		ok("/v1/en/bounties/portgas-d-ace-550").jsonPath("$.initialKnowledge.inactive")
			.isEqualTo(false)
			.jsonPath("$.reveals[0].inactive")
			.isEqualTo(true);
	}

	@Test
	void aContentWithoutRevealsHasNoInitialKnowledge() {
		ok("/v1/en/characters/monkey-d-luffy").jsonPath("$.initialKnowledge").isEmpty().jsonPath("$.reveals").isEmpty();
	}

	@Test
	void theFilteredPathKeepsTheSameShape() {
		ok("/v1/en/characters/monkey-d-luffy?asOfChapter=2").jsonPath("$.description[0].text")
			.isEqualTo("Description of Monkey D. Luffy")
			.jsonPath("$.gallery[0].kind")
			.isEqualTo("APPEARANCE")
			.jsonPath("$.devilFruits[0].former")
			.isEqualTo(false);
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return this.client.get().uri(uri).exchange().expectStatus().isOk().expectBody();
	}

	private static Translation en(String name) {
		return Translation.of("en", name);
	}

}
