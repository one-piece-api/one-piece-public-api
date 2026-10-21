package dev.onepieceapi.publicapi;

import dev.onepieceapi.publicapi.PublishedContentFixture.Persona;
import dev.onepieceapi.publicapi.PublishedContentFixture.Translation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The characters end to end (Character plan C2): real views, real HTTP. Seeded for every
 * test, in Italian and English: Luffy online with portrait, birthday, epithets (one only
 * in Italian), aliases, two species (Gyojin written only in Italian) and the chapter of
 * his first appearance; Nami (Ningen) and Jinbe (Gyojin) alive, Ace (Ningen) deceased,
 * all online without portrait or lists beyond their species; Usopp in draft, with a
 * portrait of his own.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiCharacterIntegrationTest extends PublishedDatabaseTest {

	private static final byte[] PNG_BYTES = { (byte) 0x89, 0x50, 0x4E, 0x47, 7, 8, 9 };

	private static final String LUFFY_IT = "Monkey D. Rufy";

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID luffy;

	private UUID humans;

	private UUID fishMen;

	private UUID chapter;

	private String portrait;

	private String draftPortrait;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		UUID saga = this.fixture.publishSaga(1, Translation.of("it", "Saga"), Translation.of("en", "Saga"));
		UUID arc = this.fixture.publishArc(1, saga, Translation.of("it", "Arco"), Translation.of("en", "Arc"));
		this.chapter = this.fixture.publishChapter(1, arc, null, null, null, Translation.of("it", "Romance Dawn"),
				Translation.of("en", "Romance Dawn"));
		this.portrait = this.fixture.image('a', PNG_BYTES);
		this.draftPortrait = this.fixture.image('b', PNG_BYTES);
		this.humans = this.fixture.publishSpecies("Ningen", null, Translation.of("it", "Umani"),
				Translation.of("en", "Humans"));
		this.fishMen = this.fixture.publishSpecies("Gyojin", null, Translation.of("it", "Uomini-pesce"));
		this.luffy = this.fixture.publishCharacter(Persona.builder()
			.romaji("Monkey D. Luffy")
			.japaneseName("Monkii Dii Rufi")
			.birthday(MonthDay.of(5, 5))
			.imageId(this.portrait)
			.epithets(List.of(Map.of("it", "Cappello di paglia", "en", "Straw Hat"), Map.of("it", "Quinto imperatore")))
			.aliases(List.of("Lucy", "Mugiwara"))
			.species(List.of(this.fishMen, this.humans))
			.build(), Translation.of("it", LUFFY_IT), Translation.of("en", "Monkey D. Luffy"));
		this.fixture.firstAppearance(this.luffy, this.chapter);
		this.fixture.publishCharacter(person("Nami", "FEMALE", "ALIVE", this.humans), Translation.of("it", "Nami"),
				Translation.of("en", "Nami"));
		this.fixture.publishCharacter(person("Jinbe", "MALE", "ALIVE", this.fishMen), Translation.of("it", "Jinbe"),
				Translation.of("en", "Jinbe"));
		this.fixture.publishCharacter(person("Portgas D. Ace", "MALE", "DECEASED", this.humans),
				Translation.of("it", "Portgas D. Ace"), Translation.of("en", "Portgas D. Ace"));
		this.fixture.draftCharacter(Persona.builder().romaji("Usopp").imageId(this.draftPortrait).build(),
				Translation.of("it", "Usopp"), Translation.of("en", "Usopp"));
	}

	@Test
	void indexesTheCharacters() {
		ok("/v1").jsonPath("$.resources.characters").isEqualTo("v1/{lang}/characters");
	}

	@Test
	void listsTheCharactersOnlineByNameInTheLanguageOfThePath() {
		ok("/v1/it/characters").jsonPath("$.totalElements")
			.isEqualTo(4)
			.jsonPath("$.content[*].name")
			.isEqualTo(List.of("Jinbe", LUFFY_IT, "Nami", "Portgas D. Ace"))
			.jsonPath("$.content[1].id")
			.isEqualTo(this.luffy.toString())
			.jsonPath("$.content[1].slug")
			.isEqualTo("monkey-d-luffy")
			.jsonPath("$.content[1].romaji")
			.isEqualTo("Monkey D. Luffy")
			.jsonPath("$.content[1].status")
			.isEqualTo("ALIVE")
			.jsonPath("$.content[1].image")
			.isEqualTo("v1/images/" + this.portrait + ".png")
			.jsonPath("$.content[0].image")
			.isEmpty()
			.jsonPath("$.content[1].description")
			.doesNotExist()
			.jsonPath("$.content[1].epithets")
			.doesNotExist();
		ok("/v1/en/characters").jsonPath("$.content[1].name").isEqualTo("Monkey D. Luffy");
	}

	@Test
	void filtersByStatusAndBySpeciesIdOrSlug() {
		ok("/v1/it/characters?status=DECEASED").jsonPath("$.content[*].name").isEqualTo(List.of("Portgas D. Ace"));
		ok("/v1/it/characters?species=ningen").jsonPath("$.content[*].name")
			.isEqualTo(List.of(LUFFY_IT, "Nami", "Portgas D. Ace"));
		ok("/v1/it/characters?species=" + this.fishMen).jsonPath("$.content[*].name")
			.isEqualTo(List.of("Jinbe", LUFFY_IT));
		ok("/v1/it/characters?species=ningen&status=ALIVE").jsonPath("$.content[*].name")
			.isEqualTo(List.of(LUFFY_IT, "Nami"));
		ok("/v1/it/characters?species=unknown").jsonPath("$.totalElements").isEqualTo(0);
		get("/v1/it/characters?status=ASLEEP").expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.errorCode")
			.isEqualTo("VALIDATION_FAILED");
	}

	@Test
	void sortsAndSearchesWithTheCommonFields() {
		ok("/v1/en/characters?sort=romaji,desc").jsonPath("$.content[*].romaji")
			.isEqualTo(List.of("Portgas D. Ace", "Nami", "Monkey D. Luffy", "Jinbe"));
		ok("/v1/it/characters?q=rufy").jsonPath("$.content[*].romaji").isEqualTo(List.of("Monkey D. Luffy"));
		get("/v1/en/characters?sort=amount").expectStatus().isBadRequest();
	}

	@Test
	void showsTheDetailBySlugOrIdWithEveryList() {
		for (String idOrSlug : new String[] { "monkey-d-luffy", this.luffy.toString() }) {
			ok("/v1/it/characters/" + idOrSlug).jsonPath("$.id")
				.isEqualTo(this.luffy.toString())
				.jsonPath("$.slug")
				.isEqualTo("monkey-d-luffy")
				.jsonPath("$.romaji")
				.isEqualTo("Monkey D. Luffy")
				.jsonPath("$.japaneseName")
				.isEqualTo("Monkii Dii Rufi")
				.jsonPath("$.language")
				.isEqualTo("it")
				.jsonPath("$.name")
				.isEqualTo(LUFFY_IT)
				.jsonPath("$.description")
				.isEqualTo("Description of " + LUFFY_IT)
				.jsonPath("$.status")
				.isEqualTo("ALIVE")
				.jsonPath("$.gender")
				.isEqualTo("MALE")
				.jsonPath("$.birthday")
				.isEqualTo("--05-05")
				.jsonPath("$.image")
				.isEqualTo("v1/images/" + this.portrait + ".png")
				.jsonPath("$.epithets")
				.isEqualTo(List.of("Cappello di paglia", "Quinto imperatore"))
				.jsonPath("$.aliases")
				.isEqualTo(List.of("Lucy", "Mugiwara"))
				.jsonPath("$.species[*].name")
				.isEqualTo(List.of("Uomini-pesce", "Umani"))
				.jsonPath("$.species[1].id")
				.isEqualTo(this.humans.toString())
				.jsonPath("$.species[1].slug")
				.isEqualTo("ningen")
				.jsonPath("$.firstAppearance.chapter.id")
				.isEqualTo(this.chapter.toString())
				.jsonPath("$.firstAppearance.chapter.title")
				.isEqualTo("Romance Dawn")
				.jsonPath("$.publishedAt")
				.isNotEmpty();
		}
	}

	@Test
	void showsInEachLanguageOnlyTheEpithetsAndSpeciesWrittenInIt() {
		ok("/v1/en/characters/monkey-d-luffy").jsonPath("$.epithets")
			.isEqualTo(List.of("Straw Hat"))
			.jsonPath("$.aliases")
			.isEqualTo(List.of("Lucy", "Mugiwara"))
			.jsonPath("$.species[*].name")
			.isEqualTo(List.of("Humans"));
	}

	@Test
	void leavesTheMissingValuesNullAndTheListsEmpty() {
		ok("/v1/en/characters/nami").jsonPath("$.gender")
			.isEqualTo("FEMALE")
			.jsonPath("$.birthday")
			.isEmpty()
			.jsonPath("$.japaneseName")
			.isEmpty()
			.jsonPath("$.image")
			.isEmpty()
			.jsonPath("$.firstAppearance")
			.isEmpty()
			.jsonPath("$.epithets")
			.isEmpty()
			.jsonPath("$.aliases")
			.isEmpty()
			.jsonPath("$.species[*].slug")
			.isEqualTo(List.of("ningen"));
	}

	@Test
	void answers404ForAnUnknownOrDraftCharacterAndRedirectsAnOldSlug() {
		for (String uri : new String[] { "/v1/en/characters/usopp", "/v1/en/characters/unknown",
				"/v1/en/characters/" + UUID.randomUUID() }) {
			get(uri).expectStatus().isNotFound().expectBody().jsonPath("$.errorCode").isEqualTo("CONTENT_NOT_FOUND");
		}

		this.fixture.republishCharacter(this.luffy, Persona.builder().romaji("Monkey D. Luffy Gear 5").build(),
				Translation.of("en", "Monkey D. Luffy"));

		get("/v1/en/characters/monkey-d-luffy").expectStatus()
			.isEqualTo(HttpStatus.MOVED_PERMANENTLY)
			.expectHeader()
			.valueEquals(HttpHeaders.LOCATION, "monkey-d-luffy-gear-5");
		ok("/v1/en/characters/monkey-d-luffy-gear-5").jsonPath("$.romaji")
			.isEqualTo("Monkey D. Luffy Gear 5")
			.jsonPath("$.epithets")
			.isEmpty();
	}

	@Test
	void servesThePortraitOnlyWhileItsCharacterIsOnline() {
		byte[] body = get("/v1/images/" + this.portrait + ".png").expectStatus()
			.isOk()
			.expectHeader()
			.contentType(MediaType.IMAGE_PNG)
			.expectBody(byte[].class)
			.returnResult()
			.getResponseBody();
		assertThat(body).isEqualTo(PNG_BYTES);
		get("/v1/images/" + this.draftPortrait + ".png").expectStatus().isNotFound();

		this.fixture.retire(this.luffy);

		get("/v1/images/" + this.portrait + ".png").expectStatus().isNotFound();
		ok("/v1/en/characters").jsonPath("$.totalElements").isEqualTo(3);
	}

	private static Persona person(String romaji, String gender, String status, UUID species) {
		return Persona.builder().romaji(romaji).gender(gender).status(status).species(List.of(species)).build();
	}

	private RestTestClient.BodyContentSpec ok(String uri) {
		return get(uri).expectStatus().isOk().expectBody();
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
