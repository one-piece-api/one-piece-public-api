package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.CharacterList;
import dev.onepieceapi.publicapi.PublishedContentFixture.Having;
import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
import dev.onepieceapi.publicapi.PublishedContentFixture.Poster;
import dev.onepieceapi.publicapi.PublishedContentFixture.Related;
import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
import dev.onepieceapi.publicapi.PublishedContentFixture.Window;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Field reveals, slugs and link names at N, end to end (workflow 3.7.1, 3.8; reveals plan
 * D13, R7 S7, S10, S13). Seeded for every test: chapters 1, 5, 8 and episode 1; every
 * content met at chapter 1 (Ace also at episode 1), Luffy's second bounty at chapter 8.
 * Dragon is first the "Mystery Man" (Nazo no Otoko), revealed at chapter 5; Garp is
 * Luffy's grandparent and Dragon's parent. Ace is believed alive until chapter 5, his
 * bounty active until then. The Gomu Gomu, a Paramecia, is revealed at chapter 8 to be
 * the Hito Hito, Model: Nika, a Zoan. Luffy's 100 million bounty replaces his 30 million
 * one.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiReaderAxisRevealsIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID chapter1;

	private UUID chapter5;

	private UUID chapter8;

	private UUID episode1;

	private UUID dragon;

	private UUID garp;

	private UUID luffy;

	private UUID ace;

	private UUID gomu;

	private UUID aceBounty;

	private UUID firstLuffyBounty;

	private UUID secondLuffyBounty;

	private UUID dragonBounty;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID saga = this.fixture.publishSaga(1, en("East Blue"));
		UUID arc = this.fixture.publishArc(1, saga, en("Romance Dawn"));
		UUID volume = this.fixture.publishVolume(1, null, null, en("Volume One"));
		this.chapter1 = this.fixture.publishChapter(1, arc, volume, null, null, en("Dawn"));
		this.chapter5 = this.fixture.publishChapter(5, arc, volume, null, null, en("Storm"));
		this.chapter8 = this.fixture.publishChapter(8, arc, volume, null, null, en("Awakening"));
		this.episode1 = this.fixture.publishEpisode(1, arc, null, en("Big Entrance"));

		UUID paramecia = metAtChapter1(this.fixture.publish("Paramecia", en("Paramecia")));
		UUID zoan = metAtChapter1(this.fixture.publish("Zoan", en("Zoan")));
		this.gomu = metAtChapter1(
				this.fixture.publishFruit("Hito Hito no Mi, Model: Nika", zoan, null, en("Human-Human, Model: Nika")));
		this.fixture.devilFruitInitialKnowledge(this.gomu, "Gomu Gomu no Mi", paramecia, Map.of("en", "Gum-Gum"));
		this.fixture.devilFruitReveal(this.gomu, this.chapter8, "Awakening", "NAME", "ROMAJI", "TYPE");

		seedMonkeys();
		this.ace = metAtChapter1(this.fixture.publishCharacter(
				Persona.builder().romaji("Portgas D. Ace").status("DECEASED").build(), en("Portgas D. Ace")));
		this.fixture.firstAppearanceEpisode(this.ace, this.episode1);
		this.fixture.characterInitialKnowledge(this.ace, "ALIVE");
		this.fixture.characterReveal(this.ace, this.chapter5, "Marineford", "STATUS");
		seedBounties();
	}

	@Test
	void namesACharacterAsKnownAtNAndShowsOnlyTheRevealsReached() {
		ok("/v1/en/characters/" + this.dragon + "?asOfChapter=1").jsonPath("$.name")
			.isEqualTo("Mystery Man")
			.jsonPath("$.romaji")
			.isEqualTo("Nazo no Otoko")
			.jsonPath("$.slug")
			.isEqualTo("nazo-no-otoko")
			.jsonPath("$.initialKnowledge.name")
			.isEqualTo("Mystery Man")
			.jsonPath("$.reveals")
			.isEmpty();
		ok("/v1/en/characters/" + this.dragon + "?asOfChapter=5").jsonPath("$.name")
			.isEqualTo("Monkey D. Dragon")
			.jsonPath("$.slug")
			.isEqualTo("monkey-d-dragon")
			.jsonPath("$.initialKnowledge.name")
			.isEqualTo("Mystery Man")
			.jsonPath("$.reveals.length()")
			.isEqualTo(1)
			.jsonPath("$.reveals[0].revealed.chapter")
			.isEqualTo(5)
			.jsonPath("$.reveals[0].name")
			.isEqualTo("Monkey D. Dragon");
	}

	@Test
	void readsTheRevealsOnTheReadersAxisOnly() {
		ok("/v1/en/characters/portgas-d-ace?asOfEpisode=1").jsonPath("$.status")
			.isEqualTo("ALIVE")
			.jsonPath("$.reveals")
			.isEmpty();
	}

	@Test
	void answersTheSlugAtNMovesAKnownOneThereAndHidesAFutureOne() {
		ok("/v1/en/characters/nazo-no-otoko?asOfChapter=1").jsonPath("$.name").isEqualTo("Mystery Man");
		get("/v1/en/characters/monkey-d-dragon?asOfChapter=1").expectStatus().isNotFound();
		get("/v1/en/characters/nazo-no-otoko?asOfChapter=5").expectStatus()
			.isEqualTo(HttpStatus.MOVED_PERMANENTLY)
			.expectHeader()
			.valueEquals(HttpHeaders.LOCATION, "monkey-d-dragon?asOfChapter=5");
		ok("/v1/en/characters/monkey-d-dragon?asOfChapter=5").jsonPath("$.name").isEqualTo("Monkey D. Dragon");
		get("/v1/en/characters/nazo-no-otoko").expectStatus()
			.isEqualTo(HttpStatus.MOVED_PERMANENTLY)
			.expectHeader()
			.valueEquals(HttpHeaders.LOCATION, "monkey-d-dragon");
	}

	@Test
	void givesTheStatusAtNInTheDetailAndInTheList() {
		ok("/v1/en/characters/portgas-d-ace?asOfChapter=1").jsonPath("$.status").isEqualTo("ALIVE");
		ok("/v1/en/characters/portgas-d-ace?asOfChapter=5").jsonPath("$.status").isEqualTo("DECEASED");
		ok("/v1/en/characters?asOfChapter=1").jsonPath("$.content[?(@.romaji == 'Portgas D. Ace')].status")
			.isEqualTo(List.of("ALIVE"))
			.jsonPath("$.content[?(@.romaji == 'Nazo no Otoko')].name")
			.isEqualTo(List.of("Mystery Man"));
	}

	@Test
	void namesEveryLinkAsKnownAtN() {
		ok("/v1/en/characters/monkey-d-luffy?asOfChapter=1").jsonPath("$.relatives[1].character.name")
			.isEqualTo("Mystery Man")
			.jsonPath("$.relatives[1].character.slug")
			.isEqualTo("nazo-no-otoko")
			.jsonPath("$.devilFruits[0].devilFruit.name")
			.isEqualTo("Gum-Gum")
			.jsonPath("$.devilFruits[0].devilFruit.slug")
			.isEqualTo("gomu-gomu-no-mi");
		ok("/v1/en/characters/monkey-d-luffy?asOfChapter=8").jsonPath("$.relatives[1].character.name")
			.isEqualTo("Monkey D. Dragon")
			.jsonPath("$.devilFruits[0].devilFruit.name")
			.isEqualTo("Human-Human, Model: Nika");
		ok("/v1/en/bounties/" + this.dragonBounty + "?asOfChapter=1").jsonPath("$.holder.name")
			.isEqualTo("Mystery Man")
			.jsonPath("$.holder.slug")
			.isEqualTo("nazo-no-otoko");
	}

	@Test
	void ordersTheCharactersThatNameItAsRelativeByTheirNameAtN() {
		ok("/v1/en/characters/monkey-d-garp?asOfChapter=1").jsonPath("$.relatives[*].character.name")
			.isEqualTo(List.of("Monkey D. Luffy", "Mystery Man"));
		ok("/v1/en/characters/monkey-d-garp?asOfChapter=5").jsonPath("$.relatives[*].character.name")
			.isEqualTo(List.of("Monkey D. Dragon", "Monkey D. Luffy"));
	}

	@Test
	void namesAndTypesAFruitAsKnownAtN() {
		ok("/v1/en/devil-fruits/" + this.gomu + "?asOfChapter=1").jsonPath("$.name")
			.isEqualTo("Gum-Gum")
			.jsonPath("$.slug")
			.isEqualTo("gomu-gomu-no-mi")
			.jsonPath("$.type.name")
			.isEqualTo("Paramecia")
			.jsonPath("$.reveals")
			.isEmpty()
			.jsonPath("$.users[0].character.name")
			.isEqualTo("Monkey D. Luffy");
		ok("/v1/en/devil-fruits/" + this.gomu + "?asOfChapter=8").jsonPath("$.name")
			.isEqualTo("Human-Human, Model: Nika")
			.jsonPath("$.type.name")
			.isEqualTo("Zoan")
			.jsonPath("$.reveals.length()")
			.isEqualTo(1);
		ok("/v1/en/devil-fruits?asOfChapter=1").jsonPath("$.content[0].name")
			.isEqualTo("Gum-Gum")
			.jsonPath("$.content[0].type.name")
			.isEqualTo("Paramecia");
	}

	@Test
	void givesTheValidityAtN() {
		ok("/v1/en/bounties/" + this.aceBounty + "?asOfChapter=1").jsonPath("$.validity").isEqualTo("ACTIVE");
		ok("/v1/en/bounties/" + this.aceBounty + "?asOfChapter=5").jsonPath("$.validity")
			.isEqualTo("INACTIVE")
			.jsonPath("$.reveals.length()")
			.isEqualTo(1);
		ok("/v1/en/bounties/" + this.firstLuffyBounty + "?asOfChapter=5").jsonPath("$.validity")
			.isEqualTo("ACTIVE")
			.jsonPath("$.chain.length()")
			.isEqualTo(1);
		ok("/v1/en/bounties/" + this.firstLuffyBounty + "?asOfChapter=8").jsonPath("$.validity")
			.isEqualTo("SUPERSEDED")
			.jsonPath("$.chain[1].validity")
			.isEqualTo("SUPERSEDED");
		ok("/v1/en/characters/monkey-d-luffy?asOfChapter=5").jsonPath("$.bounties[*].validity")
			.isEqualTo(List.of("ACTIVE"));
		ok("/v1/en/bounties?asOfChapter=1").jsonPath("$.content[?(@.amount == 550000000)].validity")
			.isEqualTo(List.of("ACTIVE"));
	}

	@Test
	void showsTheFullTruthAtTheHighestChapter() {
		List<String> uris = List.of("/v1/en/characters/" + this.dragon, "/v1/en/characters/" + this.garp,
				"/v1/en/characters/" + this.luffy, "/v1/en/devil-fruits/" + this.gomu,
				"/v1/en/bounties/" + this.secondLuffyBounty, "/v1/en/bounties/" + this.aceBounty);
		for (String uri : uris) {
			assertThat(body(uri + "?asOfChapter=8")).as(uri).isEqualTo(body(uri));
		}
	}

	private void seedMonkeys() {
		this.garp = metAtChapter1(this.fixture.publishCharacter(Persona.builder().romaji("Monkey D. Garp").build(),
				en("Monkey D. Garp")));
		this.dragon = metAtChapter1(this.fixture.publishCharacter(Persona.builder()
			.romaji("Monkey D. Dragon")
			.relatives(List.of(new Related(this.garp, "PARENT")))
			.build(), en("Monkey D. Dragon")));
		this.fixture.window(this.dragon, CharacterList.RELATIVE, 0, fromChapter1());
		this.fixture.characterInitialIdentity(this.dragon, "Nazo no Otoko", "Mystery Man");
		this.fixture.characterReveal(this.dragon, this.chapter5, "Luffy's father", "IDENTITY");
		this.luffy = metAtChapter1(this.fixture.publishCharacter(Persona.builder()
			.romaji("Monkey D. Luffy")
			.relatives(List.of(new Related(this.garp, "GRANDPARENT"), new Related(this.dragon, "PARENT")))
			.devilFruits(List.of(new Having(this.gomu, false)))
			.build(), en("Monkey D. Luffy")));
		this.fixture.window(this.luffy, CharacterList.RELATIVE, 0, fromChapter1());
		this.fixture.window(this.luffy, CharacterList.RELATIVE, 1, fromChapter1());
		this.fixture.window(this.luffy, CharacterList.DEVIL_FRUIT, 0, fromChapter1());
	}

	private void seedBounties() {
		this.aceBounty = metAtChapter1(this.fixture.bounty(Poster.builder()
			.posterName("Portgas D. Ace")
			.amount(550_000_000L)
			.inactive(true)
			.holderId(this.ace)
			.build(), en("Ace")));
		this.fixture.bountyInitialKnowledge(this.aceBounty, false);
		this.fixture.bountyReveal(this.aceBounty, this.chapter5, "Death", "INACTIVE");
		this.firstLuffyBounty = metAtChapter1(this.fixture.bounty(
				Poster.builder().posterName("Monkey D. Luffy").amount(30_000_000L).holderId(this.luffy).build(),
				en("Arlong Park")));
		this.secondLuffyBounty = this.fixture.bounty(Poster.builder()
			.posterName("Monkey D. Luffy")
			.amount(100_000_000L)
			.previousId(this.firstLuffyBounty)
			.holderId(this.luffy)
			.build(), en("Alabasta"));
		this.fixture.firstAppearance(this.secondLuffyBounty, this.chapter8);
		openParagraphs(this.secondLuffyBounty);
		this.dragonBounty = metAtChapter1(this.fixture.bounty(
				Poster.builder().posterName("Monkey D. Dragon").amount(0L).holderId(this.dragon).build(),
				en("Revolutionary")));
	}

	/** Met at chapter 1, its description read from there. */
	private UUID metAtChapter1(UUID contentId) {
		this.fixture.firstAppearance(contentId, this.chapter1);
		openParagraphs(contentId);
		return contentId;
	}

	private void openParagraphs(UUID contentId) {
		this.fixture.paragraphWindow(contentId, 0, fromChapter1());
	}

	private Window fromChapter1() {
		return Window.builder().discoveredChapter(this.chapter1).build();
	}

	private static Translation en(String name) {
		return Translation.of("en", name);
	}

	private String body(String uri) {
		return get(uri).expectStatus().isOk().expectBody(String.class).returnResult().getResponseBody();
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return get(uri).expectStatus().isOk().expectBody();
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
