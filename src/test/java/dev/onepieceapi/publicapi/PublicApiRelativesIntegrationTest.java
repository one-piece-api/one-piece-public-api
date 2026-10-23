package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
import dev.onepieceapi.publicapi.PublishedContentFixture.Related;
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
 * The relatives of the characters end to end (Character plan C12): real views, real HTTP.
 * Seeded for every test, in Italian and English: Luffy, who stores Garp as his
 * grandparent, Dragon as his parent and, written only in Italian, Makino as a cousin;
 * Ace, who stores Luffy as a sworn sibling; Sabo, who stores Luffy in a draft only; and
 * Nami, who has no relatives.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiRelativesIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID garp;

	private UUID dragon;

	private UUID luffy;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		this.garp = this.fixture.publishCharacter(Persona.builder().romaji("Monkey D. Garp").build(),
				Translation.of("it", "Monkey D. Garp"), Translation.of("en", "Monkey D. Garp"));
		this.dragon = this.fixture.publishCharacter(Persona.builder().romaji("Monkey D. Dragon").build(),
				Translation.of("it", "Monkey D. Dragon"), Translation.of("en", "Monkey D. Dragon"));
		UUID makino = this.fixture.publishCharacter(Persona.builder().romaji("Makino").gender("FEMALE").build(),
				Translation.of("it", "Makino"));
		this.luffy = this.fixture.publishCharacter(Persona.builder()
			.romaji("Monkey D. Luffy")
			.relatives(List.of(new Related(this.garp, "GRANDPARENT"), new Related(this.dragon, "PARENT"),
					new Related(makino, "COUSIN")))
			.build(), Translation.of("it", "Monkey D. Rufy"), Translation.of("en", "Monkey D. Luffy"));
		this.fixture.publishCharacter(Persona.builder()
			.romaji("Portgas D. Ace")
			.relatives(List.of(new Related(this.luffy, "SWORN_SIBLING")))
			.build(), Translation.of("it", "Portuguese D. Ace"), Translation.of("en", "Portgas D. Ace"));
		this.fixture.draftCharacter(
				Persona.builder().romaji("Sabo").relatives(List.of(new Related(this.luffy, "SWORN_SIBLING"))).build(),
				Translation.of("it", "Sabo"), Translation.of("en", "Sabo"));
		this.fixture.publishCharacter(Persona.builder().romaji("Nami").gender("FEMALE").build(),
				Translation.of("it", "Nami"), Translation.of("en", "Nami"));
	}

	@Test
	void showsTheRelativesItStoresInOrderThenTheOthersByNameEachWithItsKind() {
		ok("/v1/en/characters/monkey-d-luffy").jsonPath("$.relatives[*].character.name")
			.isEqualTo(List.of("Monkey D. Garp", "Monkey D. Dragon", "Portgas D. Ace"))
			.jsonPath("$.relatives[*].kind")
			.isEqualTo(List.of("GRANDPARENT", "PARENT", "SWORN_SIBLING"))
			.jsonPath("$.relatives[0].character.id")
			.isEqualTo(this.garp.toString())
			.jsonPath("$.relatives[0].character.slug")
			.isEqualTo("monkey-d-garp")
			.jsonPath("$.relatives[0].character.gender")
			.isEqualTo("MALE");
	}

	@Test
	void leavesOutARelativeNotWrittenInTheLanguageAndADraftThatNamesTheCharacter() {
		// Makino is written in Italian only; Sabo stores Luffy in a draft only.
		ok("/v1/it/characters/monkey-d-luffy").jsonPath("$.relatives[*].character.name")
			.isEqualTo(List.of("Monkey D. Garp", "Monkey D. Dragon", "Makino", "Portuguese D. Ace"))
			.jsonPath("$.relatives[2].character.gender")
			.isEqualTo("FEMALE");
	}

	@Test
	void showsTheLinkFromTheOtherSideThroughTheInverseKind() {
		ok("/v1/en/characters/monkey-d-garp").jsonPath("$.relatives.length()")
			.isEqualTo(1)
			.jsonPath("$.relatives[0].character.name")
			.isEqualTo("Monkey D. Luffy")
			.jsonPath("$.relatives[0].kind")
			.isEqualTo("GRANDCHILD");
		ok("/v1/en/characters/monkey-d-dragon").jsonPath("$.relatives[0].kind").isEqualTo("CHILD");
		ok("/v1/en/characters/portgas-d-ace").jsonPath("$.relatives[0].kind")
			.isEqualTo("SWORN_SIBLING")
			.jsonPath("$.relatives[0].character.name")
			.isEqualTo("Monkey D. Luffy");
	}

	@Test
	void aCharacterWithoutRelativesListsNone() {
		ok("/v1/en/characters/nami").jsonPath("$.relatives.length()").isEqualTo(0);
	}

	@Test
	void aRetiredCharacterIsNoLongerAnyonesRelative() {
		this.fixture.retire(this.garp);

		ok("/v1/en/characters/monkey-d-luffy").jsonPath("$.relatives[*].character.name")
			.isEqualTo(List.of("Monkey D. Dragon", "Portgas D. Ace"));
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return this.client.get().uri(uri).exchange().expectStatus().isOk().expectBody();
	}

}
