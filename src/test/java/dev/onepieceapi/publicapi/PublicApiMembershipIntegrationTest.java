package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Belonging;
import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
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
 * The memberships of the characters end to end (Character plan C8): real views, real
 * HTTP. Seeded for every test, in Italian and English: Mugiwara (a pirate crew) and
 * Marina (the Navy) online, Baroque Works online but written only in Italian, Akagami in
 * draft; Nami a Straw Hat navigator who once was a Baroque Works associate; Koby a Marine
 * captain; Zoro, a Straw Hat who is also listed in the draft crew; Usopp, a draft.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiMembershipIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID mugiwara;

	private UUID marina;

	private UUID baroque;

	private UUID nami;

	private UUID koby;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		this.mugiwara = this.fixture.publishAffiliation("Mugiwara no Ichimi", null,
				Translation.of("it", "Pirati di Cappello di Paglia"), Translation.of("en", "Straw Hat Pirates"));
		this.marina = this.fixture.publishAffiliation("Kaigun", null, Translation.of("it", "Marina"),
				Translation.of("en", "Navy"));
		this.baroque = this.fixture.publishAffiliation("Baroque Works", null, Translation.of("it", "Baroque Works"));
		UUID draft = this.fixture.publishAffiliation("Akagami Kaizokudan", null,
				Translation.of("it", "Pirati del Rosso"));
		this.fixture.retire(draft);
		this.fixture.affiliationKind(this.mugiwara, "PIRATE_CREW");
		this.fixture.affiliationKind(this.marina, "NAVY");
		this.nami = this.fixture
			.publishCharacter(
					Persona.builder()
						.romaji("Nami")
						.affiliations(List.of(new Belonging(this.baroque, Map.of(), null, true),
								new Belonging(this.mugiwara, Map.of("it", "Navigatrice", "en", "Navigator"), null,
										false),
								new Belonging(draft, Map.of(), null, false)))
						.build(),
					Translation.of("it", "Nami"), Translation.of("en", "Nami"));
		this.koby = this.fixture.publishCharacter(Persona.builder()
			.romaji("Koby")
			.affiliations(List.of(new Belonging(this.marina, Map.of("en", "Hero of the Navy"), "CAPTAIN", false)))
			.build(), Translation.of("it", "Koby"), Translation.of("en", "Koby"));
		this.fixture.publishCharacter(Persona.builder()
			.romaji("Roronoa Zoro")
			.affiliations(List.of(new Belonging(this.mugiwara, Map.of(), null, false)))
			.build(), Translation.of("it", "Roronoa Zoro"), Translation.of("en", "Roronoa Zoro"));
		this.fixture.draftCharacter(Persona.builder()
			.romaji("Usopp")
			.affiliations(List.of(new Belonging(this.mugiwara, Map.of(), null, false)))
			.build(), Translation.of("it", "Usopp"), Translation.of("en", "Usopp"));
	}

	@Test
	void showsTheMembershipsInOrderWithTheAffiliationInTheLanguageOfThePathItsRoleAndRank() {
		ok("/v1/en/characters/nami").jsonPath("$.affiliations.length()")
			.isEqualTo(1)
			.jsonPath("$.affiliations[0].affiliation.id")
			.isEqualTo(this.mugiwara.toString())
			.jsonPath("$.affiliations[0].affiliation.slug")
			.isEqualTo("mugiwara-no-ichimi")
			.jsonPath("$.affiliations[0].affiliation.name")
			.isEqualTo("Straw Hat Pirates")
			.jsonPath("$.affiliations[0].affiliation.kind")
			.isEqualTo("PIRATE_CREW")
			.jsonPath("$.affiliations[0].role")
			.isEqualTo("Navigator")
			.jsonPath("$.affiliations[0].marineRank")
			.isEmpty()
			.jsonPath("$.affiliations[0].former")
			.isEqualTo(false);
		// In the version's order: Baroque Works, which Nami left, then the Straw Hats.
		ok("/v1/it/characters/nami").jsonPath("$.affiliations[0].affiliation.name")
			.isEqualTo("Baroque Works")
			.jsonPath("$.affiliations[0].former")
			.isEqualTo(true)
			.jsonPath("$.affiliations[0].role")
			.isEmpty()
			.jsonPath("$.affiliations[1].affiliation.name")
			.isEqualTo("Pirati di Cappello di Paglia")
			.jsonPath("$.affiliations[1].role")
			.isEqualTo("Navigatrice");
	}

	@Test
	void leavesOutAnAffiliationNotWrittenInTheLanguageAndOneNotOnline() {
		// Baroque Works is written in Italian only; Akagami is retired, so it is not
		// there at all.
		ok("/v1/en/characters/nami").jsonPath("$.affiliations[*].affiliation.name")
			.isEqualTo(List.of("Straw Hat Pirates"));
	}

	@Test
	void showsTheRankOfAMarine() {
		ok("/v1/en/characters/koby").jsonPath("$.affiliations[0].marineRank")
			.isEqualTo("CAPTAIN")
			.jsonPath("$.affiliations[0].role")
			.isEqualTo("Hero of the Navy")
			.jsonPath("$.affiliations[0].affiliation.kind")
			.isEqualTo("NAVY");
		ok("/v1/it/characters/koby").jsonPath("$.affiliations[0].role").isEmpty();
	}

	@Test
	void filtersTheListByTheAffiliationTheyCurrentlyBelongToByIdOrSlug() {
		ok("/v1/en/characters?affiliation=mugiwara-no-ichimi").jsonPath("$.content[*].name")
			.isEqualTo(List.of("Nami", "Roronoa Zoro"));
		ok("/v1/en/characters?affiliation=" + this.marina).jsonPath("$.content[*].name").isEqualTo(List.of("Koby"));
		// Nami left Baroque Works.
		ok("/v1/en/characters?affiliation=" + this.baroque).jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/characters?affiliation=unknown").jsonPath("$.totalElements").isEqualTo(0);
	}

	@Test
	void filtersTheListByTheKindOfTheAffiliationTheyCurrentlyBelongTo() {
		ok("/v1/en/characters?affiliationKind=PIRATE_CREW").jsonPath("$.content[*].name")
			.isEqualTo(List.of("Nami", "Roronoa Zoro"));
		ok("/v1/en/characters?affiliationKind=NAVY").jsonPath("$.content[*].name").isEqualTo(List.of("Koby"));
		ok("/v1/en/characters?affiliationKind=NAVY&affiliation=mugiwara-no-ichimi").jsonPath("$.totalElements")
			.isEqualTo(0);
		client().get()
			.uri("/v1/en/characters?affiliationKind=CIRCUS")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("VALIDATION_FAILED");
	}

	@Test
	void anAffiliationCountsItsCurrentMembersOnline() {
		// Nami and Zoro; Usopp is a draft.
		ok("/v1/en/affiliations/mugiwara-no-ichimi").jsonPath("$.memberCount").isEqualTo(2);
		ok("/v1/en/affiliations/kaigun").jsonPath("$.memberCount").isEqualTo(1);
		// Nami left it, and nobody else ever belonged to it.
		ok("/v1/it/affiliations/baroque-works").jsonPath("$.memberCount").isEqualTo(0);
	}

	private RestTestClient client() {
		return this.client;
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return this.client.get().uri(uri).exchange().expectStatus().isOk().expectBody();
	}

}
