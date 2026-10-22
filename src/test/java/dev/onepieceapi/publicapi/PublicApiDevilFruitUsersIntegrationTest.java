package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Having;
import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
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
 * The Devil Fruits of the characters end to end (Character plan C10): real views, real
 * HTTP. Seeded for every test, in Italian and English: the Mera Mera no Mi, which Ace ate
 * and Sabo eats now, and written only in Italian the Gomu Gomu no Mi, eaten by Luffy; the
 * Smile fruit, shared by two characters, and Teach with the Yami Yami no Mi in a draft.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiDevilFruitUsersIntegrationTest extends PublishedDatabaseTest {

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID mera;

	private UUID gomu;

	private UUID smile;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID paramecia = this.fixture.publish("Paramecia", Translation.of("it", "Paramecia"),
				Translation.of("en", "Paramecia"));
		UUID logia = this.fixture.publish("Logia", Translation.of("it", "Logia"), Translation.of("en", "Logia"));
		this.mera = this.fixture.publishFruit("Mera Mera no Mi", logia, null, Translation.of("it", "Foco Foco"),
				Translation.of("en", "Flame-Flame"));
		this.gomu = this.fixture.publishFruit("Gomu Gomu no Mi", paramecia, null, Translation.of("it", "Gom Gom"));
		this.smile = this.fixture.publishFruit("Smile", paramecia, null, Translation.of("it", "Smile"),
				Translation.of("en", "Smile"));
		UUID draft = this.fixture.publishFruit("Yami Yami no Mi", logia, null, Translation.of("it", "Buio Buio"));
		this.fixture.multipleUsers(this.smile);
		this.fixture.publishCharacter(
				Persona.builder().romaji("Portgas D. Ace").devilFruits(List.of(new Having(this.mera, true))).build(),
				Translation.of("it", "Portuguese D. Ace"), Translation.of("en", "Portgas D. Ace"));
		this.fixture.publishCharacter(Persona.builder()
			.romaji("Sabo")
			.devilFruits(List.of(new Having(this.mera, false), new Having(this.gomu, true)))
			.build(), Translation.of("it", "Sabo"), Translation.of("en", "Sabo"));
		this.fixture.publishCharacter(
				Persona.builder().romaji("Monkey D. Luffy").devilFruits(List.of(new Having(this.gomu, false))).build(),
				Translation.of("it", "Monkey D. Rufy"), Translation.of("en", "Monkey D. Luffy"));
		this.fixture.publishCharacter(
				Persona.builder().romaji("Caesar Clown").devilFruits(List.of(new Having(this.smile, false))).build(),
				Translation.of("it", "Caesar Clown"), Translation.of("en", "Caesar Clown"));
		this.fixture.publishCharacter(
				Persona.builder().romaji("Wanze").devilFruits(List.of(new Having(this.smile, false))).build(),
				Translation.of("it", "Wanze"));
		this.fixture.publishCharacter(Persona.builder().romaji("Nami").build(), Translation.of("it", "Nami"),
				Translation.of("en", "Nami"));
		this.fixture.draftCharacter(
				Persona.builder().romaji("Marshall D. Teach").devilFruits(List.of(new Having(draft, false))).build(),
				Translation.of("it", "Marshall D. Teach"), Translation.of("en", "Marshall D. Teach"));
		this.fixture.retire(draft);
	}

	@Test
	void showsTheFruitsOfACharacterInOrderWithTheNameInTheLanguageOfThePath() {
		ok("/v1/en/characters/sabo").jsonPath("$.devilFruits.length()")
			.isEqualTo(1)
			.jsonPath("$.devilFruits[0].devilFruit.id")
			.isEqualTo(this.mera.toString())
			.jsonPath("$.devilFruits[0].devilFruit.slug")
			.isEqualTo("mera-mera-no-mi")
			.jsonPath("$.devilFruits[0].devilFruit.name")
			.isEqualTo("Flame-Flame")
			.jsonPath("$.devilFruits[0].former")
			.isEqualTo(false);
		// The Gomu Gomu no Mi is not written in English: left out, as the second entry.
		ok("/v1/it/characters/sabo").jsonPath("$.devilFruits[*].devilFruit.name")
			.isEqualTo(List.of("Foco Foco", "Gom Gom"))
			.jsonPath("$.devilFruits[1].former")
			.isEqualTo(true);
	}

	@Test
	void aCharacterWithoutFruitsListsNone() {
		ok("/v1/en/characters/nami").jsonPath("$.devilFruits.length()").isEqualTo(0);
	}

	@Test
	void showsTheUsersOfAFruitTheCurrentOnesFirstThenByName() {
		ok("/v1/en/devil-fruits/mera-mera-no-mi").jsonPath("$.multipleUsers")
			.isEqualTo(false)
			.jsonPath("$.users[*].character.name")
			.isEqualTo(List.of("Sabo", "Portgas D. Ace"))
			.jsonPath("$.users[0].former")
			.isEqualTo(false)
			.jsonPath("$.users[0].character.slug")
			.isEqualTo("sabo")
			.jsonPath("$.users[1].former")
			.isEqualTo(true);
	}

	@Test
	void leavesOutAUserNotWrittenInTheLanguageAndOneNotOnline() {
		// Wanze is written in Italian only, Teach is a draft: neither is a user online.
		ok("/v1/en/devil-fruits/smile").jsonPath("$.users[*].character.name").isEqualTo(List.of("Caesar Clown"));
		ok("/v1/it/devil-fruits/smile").jsonPath("$.users[*].character.name")
			.isEqualTo(List.of("Caesar Clown", "Wanze"));
	}

	@Test
	void flagsTheFruitsWithSeveralUsers() {
		ok("/v1/en/devil-fruits/smile").jsonPath("$.multipleUsers").isEqualTo(true);
		ok("/v1/it/devil-fruits/gomu-gomu-no-mi").jsonPath("$.multipleUsers").isEqualTo(false);
	}

	@Test
	void filtersTheCharactersByAFruitTheyHaveNowOrOnceByIdOrSlug() {
		ok("/v1/en/characters?devilFruit=mera-mera-no-mi").jsonPath("$.content[*].name")
			.isEqualTo(List.of("Portgas D. Ace", "Sabo"));
		ok("/v1/it/characters?devilFruit=" + this.gomu).jsonPath("$.content[*].name")
			.isEqualTo(List.of("Monkey D. Rufy", "Sabo"));
		ok("/v1/en/characters?devilFruit=unknown").jsonPath("$.totalElements").isEqualTo(0);
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return this.client.get().uri(uri).exchange().expectStatus().isOk().expectBody();
	}

}
