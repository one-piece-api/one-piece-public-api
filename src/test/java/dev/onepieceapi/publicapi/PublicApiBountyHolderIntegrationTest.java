package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
import dev.onepieceapi.publicapi.PublishedContentFixture.Poster;
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
 * The holder of a bounty and the bounties of a character end to end (Character plan C6):
 * real views, real HTTP. Seeded for every test: Luffy (written in Italian and English)
 * holds 30M, superseded by 100M, and a draft of 5B that is not online; Nami, written in
 * English only, holds 16M; Zoro's 120M has no holder.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiBountyHolderIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID luffy;

	private UUID nami;

	private UUID luffy100;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		this.luffy = this.fixture.publishCharacter(Persona.builder().romaji("Monkey D. Luffy").build(),
				Translation.of("it", "Monkey D. Rufy"), Translation.of("en", "Monkey D. Luffy"));
		this.nami = this.fixture.publishCharacter(Persona.builder().romaji("Nami").build(),
				Translation.of("en", "Nami"));
		UUID luffy30 = bounty("Monkey D. Luffy", 30_000_000L, null, this.luffy, null);
		this.luffy100 = bounty("Monkey D. Luffy", 100_000_000L, luffy30, this.luffy, null);
		bounty("Monkey D. Luffy", 5_000_000_000L, null, this.luffy, "DRAFT");
		bounty("Nami", 16_000_000L, null, this.nami, null);
		bounty("Roronoa Zoro", 120_000_000L, null, null, null);
	}

	@Test
	void aRowSaysItsHolderNamedInTheLanguageOfThePath() {
		ok("/v1/it/bounties").jsonPath("$.content[1].holder.name")
			.isEqualTo("Monkey D. Rufy")
			.jsonPath("$.content[1].holder.id")
			.isEqualTo(this.luffy.toString())
			.jsonPath("$.content[1].holder.slug")
			.isEqualTo("monkey-d-luffy");
		ok("/v1/en/bounties").jsonPath("$.content[1].holder.name").isEqualTo("Monkey D. Luffy");
	}

	@Test
	void aBountyWithoutHolderSaysNullAndOneWhoseHolderIsNotWrittenInTheLanguageKeepsTheIdOnly() {
		ok("/v1/en/bounties").jsonPath("$.content[0].posterName")
			.isEqualTo("Roronoa Zoro")
			.jsonPath("$.content[0].holder")
			.isEmpty()
			.jsonPath("$.content[3].holder.name")
			.isEqualTo("Nami");
		ok("/v1/it/bounties").jsonPath("$.content[3].holder.id")
			.isEqualTo(this.nami.toString())
			.jsonPath("$.content[3].holder.name")
			.isEmpty()
			.jsonPath("$.content[3].holder.slug")
			.isEmpty();
	}

	@Test
	void theDetailSaysItsHolder() {
		ok("/v1/en/bounties/" + this.luffy100).jsonPath("$.holder.id")
			.isEqualTo(this.luffy.toString())
			.jsonPath("$.holder.slug")
			.isEqualTo("monkey-d-luffy")
			.jsonPath("$.holder.name")
			.isEqualTo("Monkey D. Luffy")
			.jsonPath("$.chain[0].holder.name")
			.isEqualTo("Monkey D. Luffy");
	}

	@Test
	void theListIsNarrowedByHolderByIdOrSlugAndAnUnknownOneListsNothing() {
		ok("/v1/en/bounties?holder=" + this.luffy).jsonPath("$.content[*].amount")
			.isEqualTo(List.of(100_000_000, 30_000_000));
		ok("/v1/en/bounties?holder=monkey-d-luffy").jsonPath("$.totalElements").isEqualTo(2);
		ok("/v1/en/bounties?holder=nami&validity=ACTIVE").jsonPath("$.content[*].amount")
			.isEqualTo(List.of(16_000_000));
		ok("/v1/en/bounties?holder=nobody").jsonPath("$.totalElements").isEqualTo(0);
		ok("/v1/en/bounties?holder=" + UUID.randomUUID()).jsonPath("$.totalElements").isEqualTo(0);
	}

	@Test
	void aCharacterListsItsBountiesOnlineHighestFirstWithTheirValidity() {
		ok("/v1/en/characters/" + this.luffy).jsonPath("$.bounties[*].amount")
			.isEqualTo(List.of(100_000_000, 30_000_000))
			.jsonPath("$.bounties[*].validity")
			.isEqualTo(List.of("ACTIVE", "SUPERSEDED"))
			.jsonPath("$.bounties[0].id")
			.isEqualTo(this.luffy100.toString())
			.jsonPath("$.bounties[0].slug")
			.isEqualTo("monkey-d-luffy-100000000");
		ok("/v1/it/characters/monkey-d-luffy").jsonPath("$.bounties.length()").isEqualTo(2);
	}

	@Test
	void aCharacterWithNoBountyHasAnEmptyList() {
		UUID zoro = this.fixture.publishCharacter(Persona.builder().romaji("Roronoa Zoro").build(),
				Translation.of("it", "Roronoa Zoro"), Translation.of("en", "Roronoa Zoro"));

		ok("/v1/en/characters/" + zoro).jsonPath("$.bounties").isEqualTo(List.of());
	}

	private UUID bounty(String posterName, long amount, UUID previous, UUID holder, String status) {
		var seed = Poster.builder()
			.posterName(posterName)
			.amount(amount)
			.previousId(previous)
			.holderId(holder)
			.status(status)
			.build();
		return this.fixture.bounty(seed, Translation.of("it", posterName + " it"),
				Translation.of("en", posterName + " en"));
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return this.client.get().uri(uri).exchange().expectStatus().isOk().expectBody();
	}

}
