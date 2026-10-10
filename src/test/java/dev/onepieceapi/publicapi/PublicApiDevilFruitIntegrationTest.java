package dev.onepieceapi.publicapi;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Devil Fruits and their images end to end (Devil Fruit plan D11 - D13): real views,
 * real HTTP. Seeded for every test: the types Logia and Zoan online, in Italian and
 * English.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PublicApiDevilFruitIntegrationTest extends PublishedDatabaseTest {

	private static final byte[] PNG_BYTES = { (byte) 0x89, 0x50, 0x4E, 0x47, 1, 2, 3 };

	private final PublishedContentFixture fixture = new PublishedContentFixture(POSTGRES);

	@LocalServerPort
	private int serverPort;

	private RestTestClient client;

	private UUID logia;

	private UUID zoan;

	@BeforeEach
	void setUp() {
		this.fixture.clear();
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.serverPort).build();
		this.logia = this.fixture.publish("Logia", Translation.of("it", "Logia IT"), Translation.of("en", "Logia EN"));
		this.zoan = this.fixture.publish("Zoan", Translation.of("it", "Zoan IT"), Translation.of("en", "Zoan EN"));
	}

	@Test
	void indexesTheFruits() {
		get("/v1").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.resources.devil-fruits")
			.isEqualTo("v1/{lang}/devil-fruits");
	}

	@Test
	void servesAFruitWithItsTypeAndImageInTheLanguageOfThePath() {
		String image = this.fixture.image('a', PNG_BYTES);
		UUID mera = this.fixture.publishFruit("Mera Mera no Mi", this.logia, image, Translation.of("it", "Foco Foco"),
				Translation.of("en", "Flame-Flame"));

		get("/v1/it/devil-fruits/mera-mera-no-mi").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.id")
			.isEqualTo(mera.toString())
			.jsonPath("$.name")
			.isEqualTo("Foco Foco")
			.jsonPath("$.description")
			.isEqualTo("Description of Foco Foco")
			.jsonPath("$.type.id")
			.isEqualTo(this.logia.toString())
			.jsonPath("$.type.slug")
			.isEqualTo("logia")
			.jsonPath("$.type.name")
			.isEqualTo("Logia IT")
			.jsonPath("$.image")
			.isEqualTo("v1/images/" + image + ".png")
			.jsonPath("$.publishedAt")
			.isEqualTo("2026-10-02T14:31:07Z");
		get("/v1/en/devil-fruits/" + mera).expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.type.name")
			.isEqualTo("Logia EN");
	}

	@Test
	void showsTheSubcategoryOfAFruitInTheLanguageOfThePath() {
		UUID mythical = this.fixture.subcategory(this.zoan, 1, Translation.of("it", "Mitico"),
				Translation.of("en", "Mythical"));
		this.fixture.subcategory(this.zoan, 0, Translation.of("it", "Antico"), Translation.of("en", "Ancient"));
		this.fixture.publishFruitOfSubcategory("Inu Inu no Mi", this.zoan, mythical, Translation.of("it", "Cane Cane"),
				Translation.of("en", "Dog-Dog"));

		get("/v1/it/devil-fruits/inu-inu-no-mi").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.subcategory.id")
			.isEqualTo(mythical.toString())
			.jsonPath("$.subcategory.name")
			.isEqualTo("Mitico")
			.jsonPath("$.subcategory.description")
			.isEqualTo("Description of Mitico");
		get("/v1/en/devil-fruits").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.content[0].subcategory.id")
			.isEqualTo(mythical.toString())
			.jsonPath("$.content[0].subcategory.name")
			.isEqualTo("Mythical")
			.jsonPath("$.content[0].subcategory.description")
			.doesNotExist();
	}

	@Test
	void showsNoSubcategoryForAFruitWithoutOrWithoutTextInThatLanguage() {
		UUID englishOnly = this.fixture.subcategory(this.zoan, 0, Translation.of("en", "Artificial"));
		this.fixture.publishFruit("Mera Mera no Mi", this.logia, null, Translation.of("en", "Flame-Flame"));
		this.fixture.publishFruitOfSubcategory("Inu Inu no Mi", this.zoan, englishOnly, Translation.of("it", "Cane"),
				Translation.of("en", "Dog-Dog"));

		get("/v1/en/devil-fruits/mera-mera-no-mi").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.subcategory")
			.isEmpty();
		get("/v1/it/devil-fruits/inu-inu-no-mi").expectStatus().isOk().expectBody().jsonPath("$.subcategory").isEmpty();
		get("/v1/en/devil-fruits/inu-inu-no-mi").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.subcategory.name")
			.isEqualTo("Artificial");
	}

	@Test
	void aTypeListsItsSubcategoriesInOrderAndEmptyWhenItHasNone() {
		this.fixture.subcategory(this.zoan, 1, Translation.of("en", "Mythical"));
		this.fixture.subcategory(this.zoan, 0, Translation.of("en", "Ancient"));

		get("/v1/en/devil-fruit-types/zoan").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.subcategories.length()")
			.isEqualTo(2)
			.jsonPath("$.subcategories[0].name")
			.isEqualTo("Ancient")
			.jsonPath("$.subcategories[1].name")
			.isEqualTo("Mythical")
			.jsonPath("$.subcategories[1].description")
			.isEqualTo("Description of Mythical");
		get("/v1/en/devil-fruit-types/logia").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.subcategories.length()")
			.isEqualTo(0);
		get("/v1/it/devil-fruit-types/zoan").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.subcategories.length()")
			.isEqualTo(0);
	}

	@Test
	void showsNoImageForAFruitWithout() {
		this.fixture.publishFruit("Gomu Gomu no Mi", this.zoan, null, Translation.of("it", "Gom Gom"),
				Translation.of("en", "Gum-Gum"));

		get("/v1/en/devil-fruits/gomu-gomu-no-mi").expectStatus().isOk().expectBody().jsonPath("$.image").isEmpty();
	}

	@Test
	void listsTheFruitsWithTheirTypeAndFiltersByTypeIdOrSlug() {
		this.fixture.publishFruit("Mera Mera no Mi", this.logia, null, Translation.of("en", "Flame-Flame"));
		this.fixture.publishFruit("Hie Hie no Mi", this.logia, null, Translation.of("en", "Chilly-Chilly"));
		this.fixture.publishFruit("Inu Inu no Mi", this.zoan, null, Translation.of("en", "Dog-Dog"));
		this.fixture.draftFruit("Yami Yami no Mi", this.logia, null, Translation.of("en", "Dark-Dark"));

		get("/v1/en/devil-fruits").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(3)
			.jsonPath("$.content[0].name")
			.isEqualTo("Chilly-Chilly")
			.jsonPath("$.content[0].type.name")
			.isEqualTo("Logia EN")
			.jsonPath("$.content[0].image")
			.isEmpty();
		for (String type : new String[] { "logia", this.logia.toString() }) {
			get("/v1/en/devil-fruits?type=" + type + "&sort=romaji,desc").expectStatus()
				.isOk()
				.expectBody()
				.jsonPath("$.totalElements")
				.isEqualTo(2)
				.jsonPath("$.content[0].romaji")
				.isEqualTo("Mera Mera no Mi")
				.jsonPath("$.content[1].romaji")
				.isEqualTo("Hie Hie no Mi");
		}
		get("/v1/en/devil-fruits?type=paramecia").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(0);
		get("/v1/en/devil-fruits?type=").expectStatus().isOk().expectBody().jsonPath("$.totalElements").isEqualTo(3);
	}

	@Test
	void filtersByAnOldSlugOfTheType() {
		this.fixture.republishWithRomaji(this.zoan, "Zoan-kei", Translation.of("en", "Zoan EN"));
		this.fixture.publishFruit("Inu Inu no Mi", this.zoan, null, Translation.of("en", "Dog-Dog"));

		get("/v1/en/devil-fruits?type=zoan").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.totalElements")
			.isEqualTo(1)
			.jsonPath("$.content[0].type.slug")
			.isEqualTo("zoan-kei");
	}

	@Test
	void listsTheFruitsOfATypeInItsDetailByName() {
		String image = this.fixture.image('b', PNG_BYTES);
		this.fixture.publishFruit("Mera Mera no Mi", this.logia, image, Translation.of("it", "Foco Foco"));
		this.fixture.publishFruit("Hie Hie no Mi", this.logia, null, Translation.of("it", "Ghiaccio Ghiaccio"));
		this.fixture.publishFruit("Inu Inu no Mi", this.zoan, null, Translation.of("it", "Cane Cane"));
		this.fixture.draftFruit("Yami Yami no Mi", this.logia, null, Translation.of("it", "Buio Buio"));

		get("/v1/it/devil-fruit-types/logia").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.devilFruits.length()")
			.isEqualTo(2)
			.jsonPath("$.devilFruits[0].name")
			.isEqualTo("Foco Foco")
			.jsonPath("$.devilFruits[0].slug")
			.isEqualTo("mera-mera-no-mi")
			.jsonPath("$.devilFruits[0].image")
			.isEqualTo("v1/images/" + image + ".png")
			.jsonPath("$.devilFruits[0].type")
			.doesNotExist()
			.jsonPath("$.devilFruits[1].name")
			.isEqualTo("Ghiaccio Ghiaccio");
		get("/v1/en/devil-fruit-types/zoan").expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.devilFruits.length()")
			.isEqualTo(0);
	}

	@Test
	void servesTheImageOfAFruitOnlineAsImmutable() {
		String image = this.fixture.image('c', PNG_BYTES);
		this.fixture.publishFruit("Mera Mera no Mi", this.logia, image, Translation.of("en", "Flame-Flame"));

		byte[] body = get("/v1/images/" + image + ".png").expectStatus()
			.isOk()
			.expectHeader()
			.contentType(MediaType.IMAGE_PNG)
			.expectHeader()
			.valueEquals(HttpHeaders.CACHE_CONTROL, "max-age=31536000, public, immutable")
			.expectHeader()
			.valueEquals("X-Content-Type-Options", "nosniff")
			.expectHeader()
			.doesNotExist(HttpHeaders.ETAG)
			.expectBody(byte[].class)
			.returnResult()
			.getResponseBody();
		assertThat(body).isEqualTo(PNG_BYTES);
	}

	@Test
	void answers404ForAnImageNotOnline() {
		String draftImage = this.fixture.image('d', PNG_BYTES);
		this.fixture.draftFruit("Yami Yami no Mi", this.logia, draftImage, Translation.of("en", "Dark-Dark"));

		for (String id : new String[] { draftImage, "e".repeat(64), "not-an-id", "A".repeat(64) }) {
			this.client.get()
				.uri("/v1/images/" + id + ".png")
				.accept(MediaType.IMAGE_PNG, MediaType.ALL)
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.NOT_FOUND)
				.expectBody()
				.jsonPath("$.errorCode")
				.isEqualTo("CONTENT_NOT_FOUND");
		}
	}

	private RestTestClient.ResponseSpec get(String uri) {
		return this.client.get().uri(uri).exchange();
	}

}
