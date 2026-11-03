package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Belonging;
import dev.onepieceapi.publicapi.PublishedContentFixture.CharacterList;
import dev.onepieceapi.publicapi.PublishedContentFixture.Having;
import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
import dev.onepieceapi.publicapi.PublishedContentFixture.Related;
import dev.onepieceapi.publicapi.PublishedContentFixture.Stay;
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
 * List entries and membership periods at N, end to end (workflow 3.7.2, 3.8; reveals plan
 * R7b): hidden before their window, former from its end, points trimmed to the reader's
 * axis. Seeded for every test: chapters 1, 2, 5, 8 and episodes 1, 4; every content met
 * at chapter 1, the Rocks Pirates at chapter 5. Usopp: Straw Hats from chapter 1 (and
 * episode 1) to chapter 5, again from chapter 8; Rocks discovered already ended at
 * chapter 5; alias Sogeking from chapter 8; his species from chapter 5 and episode 1. The
 * Mera Mera: Ace from chapter 1 to chapter 8, Sabo from chapter 2. Luffy's grandfather
 * Garp from chapter 5.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiReaderAxisWindowsIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID chapter1;

	private UUID chapter2;

	private UUID chapter5;

	private UUID chapter8;

	private UUID episode1;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID saga = this.fixture.publishSaga(1, en("East Blue"));
		UUID arc1 = this.fixture.publishArc(1, saga, en("Romance Dawn"));
		UUID arc2 = this.fixture.publishArc(2, saga, en("Syrup Village"));
		UUID volume = this.fixture.publishVolume(1, null, null, en("Volume One"));
		this.chapter1 = this.fixture.publishChapter(1, arc1, volume, null, null, en("Dawn"));
		this.chapter2 = this.fixture.publishChapter(2, arc1, volume, null, null, en("Straw Hat"));
		this.chapter5 = this.fixture.publishChapter(5, arc2, volume, null, null, en("Liar"));
		this.chapter8 = this.fixture.publishChapter(8, arc2, volume, null, null, en("Sogeking"));
		this.episode1 = this.fixture.publishEpisode(1, arc1, null, en("Big Entrance"));
		this.fixture.publishEpisode(4, arc2, null, en("Captain Usopp"));

		UUID strawHats = metAtChapter1(this.fixture.publishAffiliation("Mugiwara no Ichimi", null, en("Straw Hats")));
		this.fixture.firstAppearanceEpisode(strawHats, this.episode1);
		UUID rocks = this.fixture.publishAffiliation("Rocks Kaizokudan", null, en("Rocks Pirates"));
		this.fixture.firstAppearance(rocks, this.chapter5);
		UUID human = metAtChapter1(this.fixture.publishSpecies("Ningen", null, en("Human")));
		this.fixture.firstAppearanceEpisode(human, this.episode1);
		UUID paramecia = metAtChapter1(this.fixture.publish("Paramecia", en("Paramecia")));
		UUID mera = metAtChapter1(this.fixture.publishFruit("Mera Mera no Mi", paramecia, null, en("Flame-Flame")));

		seedUsopp(strawHats, rocks, human);
		seedFruitUsers(mera);
		UUID garp = metAtChapter1(this.fixture.publishCharacter(Persona.builder().romaji("Monkey D. Garp").build(),
				en("Monkey D. Garp")));
		UUID luffy = metAtChapter1(this.fixture.publishCharacter(Persona.builder()
			.romaji("Monkey D. Luffy")
			.relatives(List.of(new Related(garp, "GRANDPARENT")))
			.build(), en("Monkey D. Luffy")));
		this.fixture.window(luffy, CharacterList.RELATIVE, 0,
				Window.builder().discoveredChapter(this.chapter5).build());
	}

	@Test
	void showsAMembershipPeriodFromItsDiscoveryAndFormerFromItsEnd() {
		ok("/v1/en/characters/usopp?asOfChapter=1").jsonPath("$.affiliations.length()")
			.isEqualTo(1)
			.jsonPath("$.affiliations[0].former")
			.isEqualTo(false)
			.jsonPath("$.affiliations[0].periods.length()")
			.isEqualTo(1)
			.jsonPath("$.affiliations[0].periods[0].discovered.chapter")
			.isEqualTo(1)
			.jsonPath("$.affiliations[0].periods[0].discovered.episode")
			.isEmpty()
			.jsonPath("$.affiliations[0].periods[0].ended")
			.isEmpty();
		ok("/v1/en/characters/usopp?asOfChapter=5").jsonPath("$.affiliations.length()")
			.isEqualTo(2)
			.jsonPath("$.affiliations[0].former")
			.isEqualTo(true)
			.jsonPath("$.affiliations[0].periods[0].ended.chapter")
			.isEqualTo(5)
			.jsonPath("$.affiliations[1].affiliation.name")
			.isEqualTo("Rocks Pirates")
			.jsonPath("$.affiliations[1].former")
			.isEqualTo(true);
		ok("/v1/en/characters/usopp?asOfChapter=8").jsonPath("$.affiliations[0].former")
			.isEqualTo(false)
			.jsonPath("$.affiliations[0].periods.length()")
			.isEqualTo(2);
	}

	@Test
	void readsOnlyThePartsOnTheReadersAxis() {
		ok("/v1/en/characters/usopp?asOfEpisode=4").jsonPath("$.affiliations.length()")
			.isEqualTo(1)
			.jsonPath("$.affiliations[0].former")
			.isEqualTo(false)
			.jsonPath("$.affiliations[0].periods.length()")
			.isEqualTo(1)
			.jsonPath("$.affiliations[0].periods[0].discovered.chapter")
			.isEmpty()
			.jsonPath("$.affiliations[0].periods[0].discovered.episode")
			.isEqualTo(1)
			.jsonPath("$.affiliations[0].periods[0].ended")
			.isEmpty()
			.jsonPath("$.species.length()")
			.isEqualTo(1);
		ok("/v1/en/characters/usopp").jsonPath("$.affiliations[0].periods.length()")
			.isEqualTo(2)
			.jsonPath("$.affiliations[0].periods[0].discovered.episode")
			.isEqualTo(1)
			.jsonPath("$.affiliations[0].periods[0].ended.chapter")
			.isEqualTo(5)
			.jsonPath("$.aliases.length()")
			.isEqualTo(1);
	}

	@Test
	void hidesAnEntryUntilItsWindowIsDiscovered() {
		ok("/v1/en/characters/usopp?asOfChapter=5").jsonPath("$.aliases.length()")
			.isEqualTo(0)
			.jsonPath("$.epithets.length()")
			.isEqualTo(1)
			.jsonPath("$.species[0].species.name")
			.isEqualTo("Human");
		ok("/v1/en/characters/usopp?asOfChapter=2").jsonPath("$.species.length()").isEqualTo(0);
		ok("/v1/en/characters/usopp?asOfChapter=8").jsonPath("$.aliases[0].value").isEqualTo("Sogeking");
		ok("/v1/en/characters/monkey-d-garp?asOfChapter=2").jsonPath("$.relatives.length()").isEqualTo(0);
		ok("/v1/en/characters/monkey-d-garp?asOfChapter=5").jsonPath("$.relatives[0].character.name")
			.isEqualTo("Monkey D. Luffy")
			.jsonPath("$.relatives[0].kind")
			.isEqualTo("GRANDCHILD");
	}

	@Test
	void ordersTheUsersOfAFruitByTheirStateAtN() {
		ok("/v1/en/devil-fruits/mera-mera-no-mi").jsonPath("$.users[0].character.name")
			.isEqualTo("Sabo")
			.jsonPath("$.users[1].former")
			.isEqualTo(true);
		ok("/v1/en/devil-fruits/mera-mera-no-mi?asOfChapter=1").jsonPath("$.users.length()").isEqualTo(1);
		ok("/v1/en/devil-fruits/mera-mera-no-mi?asOfChapter=5").jsonPath("$.users[0].character.name")
			.isEqualTo("Portgas D. Ace")
			.jsonPath("$.users[0].former")
			.isEqualTo(false)
			.jsonPath("$.users[0].ended")
			.isEmpty()
			.jsonPath("$.users[1].character.name")
			.isEqualTo("Sabo");
		ok("/v1/en/devil-fruits/mera-mera-no-mi?asOfChapter=8").jsonPath("$.users[0].character.name")
			.isEqualTo("Sabo")
			.jsonPath("$.users[1].former")
			.isEqualTo(true)
			.jsonPath("$.users[1].ended.chapter")
			.isEqualTo(8);
		ok("/v1/en/characters/portgas-d-ace?asOfChapter=8").jsonPath("$.devilFruits[0].former").isEqualTo(true);
	}

	private void seedUsopp(UUID strawHats, UUID rocks, UUID human) {
		UUID usopp = metAtChapter1(
				this.fixture.publishCharacter(Persona.builder()
					.romaji("Usopp")
					.epithets(List.of(Map.of("en", "King of Snipers")))
					.aliases(List.of("Sogeking"))
					.species(List.of(human))
					.affiliations(List.of(
							new Belonging(strawHats, List.of(new Stay(null, null, false), new Stay(null, null, false))),
							new Belonging(rocks, null, null, false)))
					.build(), en("Usopp")));
		this.fixture.firstAppearanceEpisode(usopp, this.episode1);
		this.fixture.periodWindow(usopp, 0, 0,
				Window.builder()
					.discoveredChapter(this.chapter1)
					.discoveredEpisode(this.episode1)
					.endedChapter(this.chapter5)
					.build());
		this.fixture.periodWindow(usopp, 0, 1, Window.builder().discoveredChapter(this.chapter8).build());
		this.fixture.periodWindow(usopp, 1, 0,
				Window.builder().discoveredChapter(this.chapter5).endedChapter(this.chapter5).build());
		this.fixture.window(usopp, CharacterList.EPITHET, 0,
				Window.builder().discoveredChapter(this.chapter1).discoveredEpisode(this.episode1).build());
		this.fixture.window(usopp, CharacterList.ALIAS, 0, Window.builder().discoveredChapter(this.chapter8).build());
		this.fixture.window(usopp, CharacterList.SPECIES, 0,
				Window.builder().discoveredChapter(this.chapter5).discoveredEpisode(this.episode1).build());
	}

	private void seedFruitUsers(UUID mera) {
		UUID ace = metAtChapter1(this.fixture.publishCharacter(
				Persona.builder().romaji("Portgas D. Ace").devilFruits(List.of(new Having(mera, true))).build(),
				en("Portgas D. Ace")));
		this.fixture.window(ace, CharacterList.DEVIL_FRUIT, 0,
				Window.builder().discoveredChapter(this.chapter1).endedChapter(this.chapter8).build());
		UUID sabo = metAtChapter1(this.fixture.publishCharacter(
				Persona.builder().romaji("Sabo").devilFruits(List.of(new Having(mera, false))).build(), en("Sabo")));
		this.fixture.window(sabo, CharacterList.DEVIL_FRUIT, 0,
				Window.builder().discoveredChapter(this.chapter2).build());
	}

	private UUID metAtChapter1(UUID contentId) {
		this.fixture.firstAppearance(contentId, this.chapter1);
		return contentId;
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
