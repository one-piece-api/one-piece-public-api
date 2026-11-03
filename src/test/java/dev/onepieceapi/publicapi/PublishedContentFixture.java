package dev.onepieceapi.publicapi;

import lombok.Builder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * The one place that writes test data into content-service's internal tables, as their
 * owner (the reader role cannot, by design). The public API's tests are the consumer side
 * of the view contract; this is the only code that knows what is behind the views.
 */
public class PublishedContentFixture {

	/** The number of the chapter that ends a former window. */
	public static final int ENDING_CHAPTER = 900;

	private static final String AUTHOR = "fixture-author";

	private static final Entity TYPE = new Entity("DEVIL_FRUIT_TYPE", "devil_fruit_type_version");

	private static final Entity FRUIT = new Entity("DEVIL_FRUIT", "devil_fruit_version");

	private static final Entity SAGA = new Entity("SAGA", "saga_version");

	private static final Entity ARC = new Entity("ARC", "arc_version");

	private static final Entity VOLUME = new Entity("VOLUME", "volume_version");

	private static final Entity CHAPTER = new Entity("CHAPTER", "chapter_version");

	private static final Entity EPISODE = new Entity("EPISODE", "episode_version");

	private static final Entity BOUNTY = new Entity("BOUNTY", "bounty_version");

	private static final Entity AFFILIATION = new Entity("AFFILIATION", "affiliation_version");

	private static final Entity SPECIES = new Entity("SPECIES", "species_version");

	private static final Entity CHARACTER = new Entity("CHARACTER", "character_version");

	private static final String PUBLISHED = "PUBLISHED";

	/** The four points of a window row, bound by {@link Window#parameters()}. */
	private static final String WINDOW_COLUMNS = "discovered_chapter_id = :discoveredChapter,"
			+ " discovered_episode_id = :discoveredEpisode, ended_chapter_id = :endedChapter,"
			+ " ended_episode_id = :endedEpisode";

	/** The story entities whose description is a list of paragraphs. */
	private static final Set<Entity> PARAGRAPHED_STORY = Set.of(SAGA, ARC);

	private final JdbcClient owner;

	public PublishedContentFixture(PostgreSQLContainer postgres) {
		this.owner = JdbcClient
			.create(new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
	}

	/**
	 * Removes every content and image, leaving the language catalog as migrated. The
	 * bounties and the fruits go first: a type with fruits linked cannot be deleted, nor
	 * an image in use; then the characters, which name species; then the types, the
	 * affiliations and the species, which may point to the chapter of their first
	 * appearance; likewise the chapters and episodes before their arcs and volumes, and
	 * those before the sagas.
	 */
	public void clear() {
		// A bounty points to the bounty it replaces: its versions go before any content.
		this.owner.sql("DELETE FROM bounty_version").update();
		// A character points to its relatives: those links go before any character
		// content.
		this.owner.sql("DELETE FROM character_version_relative").update();
		// A paragraph of a saga or an arc may point to a chapter or an episode, which go
		// first.
		this.owner.sql("DELETE FROM saga_version_paragraph").update();
		this.owner.sql("DELETE FROM arc_version_paragraph").update();
		for (Entity entity : new Entity[] { BOUNTY, CHARACTER, FRUIT, TYPE, AFFILIATION, SPECIES, CHAPTER, EPISODE, ARC,
				VOLUME, SAGA }) {
			this.owner.sql("DELETE FROM content WHERE entity_type = :type").param("type", entity.type()).update();
		}
		this.owner.sql("DELETE FROM content").update();
		this.owner.sql("DELETE FROM image").update();
	}

	/** A Devil Fruit Type online, with its slug on record. */
	public UUID publish(String romaji, Translation... translations) {
		UUID contentId = insertContent(TYPE);
		insertOnlineVersion(TYPE, contentId, 1, romaji, translations);
		return contentId;
	}

	/**
	 * A Devil Fruit online, of an online type, with its image if any and its slug on
	 * record.
	 */
	public UUID publishFruit(String romaji, UUID typeId, String imageId, Translation... translations) {
		UUID contentId = insertContent(FRUIT);
		UUID versionId = insertOnlineVersion(FRUIT, contentId, 1, romaji, translations);
		fruitLinks(versionId, typeId, imageId);
		return contentId;
	}

	/**
	 * A subcategory of a type online, written in the given languages; returns its id.
	 * Positions are the order the type shows them in.
	 */
	public UUID subcategory(UUID typeId, int position, Translation... translations) {
		UUID subcategoryId = UUID.randomUUID();
		UUID rowId = UUID.randomUUID();
		this.owner.sql("""
				INSERT INTO devil_fruit_type_version_subcategory (id, version_id, subcategory_id, position)
				VALUES (:row, (SELECT id FROM content_version WHERE content_id = :type AND status = 'PUBLISHED'),
				        :subcategory, :position)""")
			.param("row", rowId)
			.param("type", typeId)
			.param("subcategory", subcategoryId)
			.param("position", position)
			.update();
		for (Translation translation : translations) {
			this.owner.sql("""
					INSERT INTO devil_fruit_type_version_subcategory_translation
					       (subcategory_row_id, language_code, name, description)
					VALUES (:row, :language, :name, :description)""")
				.param("row", rowId)
				.param("language", translation.language())
				.param("name", translation.name())
				.param("description", translation.description())
				.update();
		}
		return subcategoryId;
	}

	/** A Devil Fruit online that names a subcategory of its type. */
	public UUID publishFruitOfSubcategory(String romaji, UUID typeId, UUID subcategoryId, Translation... translations) {
		UUID contentId = publishFruit(romaji, typeId, null, translations);
		this.owner
			.sql("UPDATE devil_fruit_version SET subcategory_id = :subcategory WHERE version_id ="
					+ " (SELECT id FROM content_version WHERE content_id = :id AND status = 'PUBLISHED')")
			.param("subcategory", subcategoryId)
			.param("id", contentId)
			.update();
		return contentId;
	}

	/**
	 * The chapter of the first appearance of the version online of a fruit, a type, a
	 * bounty, an affiliation, a species or a character.
	 */
	public void firstAppearance(UUID contentId, UUID chapterId) {
		firstAppearanceIn("first_appearance_chapter_id", contentId, chapterId);
	}

	/**
	 * The episode of the first appearance of the version online of a fruit, a type, a
	 * bounty, an affiliation, a species or a character.
	 */
	public void firstAppearanceEpisode(UUID contentId, UUID episodeId) {
		firstAppearanceIn("first_appearance_episode_id", contentId, episodeId);
	}

	private void firstAppearanceIn(String column, UUID contentId, UUID storyItemId) {
		for (String table : new String[] { "devil_fruit_version", "devil_fruit_type_version", "bounty_version",
				"affiliation_version", "species_version", "character_version" }) {
			this.owner
				.sql("UPDATE " + table + " SET " + column + " = :item WHERE version_id ="
						+ " (SELECT id FROM content_version WHERE content_id = :id AND status = 'PUBLISHED')")
				.param("item", storyItemId)
				.param("id", contentId)
				.update();
		}
	}

	/** The chapter of the first appearance of a subcategory of a type online. */
	public void subcategoryFirstAppearance(UUID subcategoryId, UUID chapterId) {
		subcategoryFirstAppearanceIn("first_appearance_chapter_id", subcategoryId, chapterId);
	}

	/** The episode of the first appearance of a subcategory of a type online. */
	public void subcategoryFirstAppearanceEpisode(UUID subcategoryId, UUID episodeId) {
		subcategoryFirstAppearanceIn("first_appearance_episode_id", subcategoryId, episodeId);
	}

	private void subcategoryFirstAppearanceIn(String column, UUID subcategoryId, UUID storyItemId) {
		this.owner
			.sql("UPDATE devil_fruit_type_version_subcategory SET " + column + " = :item"
					+ " WHERE subcategory_id = :subcategory")
			.param("item", storyItemId)
			.param("subcategory", subcategoryId)
			.update();
	}

	/** A Devil Fruit still in draft: never online, nor its image. */
	public void draftFruit(String romaji, UUID typeId, String imageId, Translation... translations) {
		UUID versionId = insertVersion(FRUIT, insertContent(FRUIT), 1, "DRAFT", romaji, translations);
		fruitLinks(versionId, typeId, imageId);
	}

	/** A stored image whose id is the given hex digit repeated; returns the id. */
	public String image(char hexDigit, byte[] bytes) {
		return image(hexDigit, "image/webp", bytes);
	}

	/** An image stored in this format, e.g. a PNG stored before WebP. */
	public String image(char hexDigit, String contentType, byte[] bytes) {
		String id = String.valueOf(hexDigit).repeat(64);
		this.owner.sql("""
				INSERT INTO image (id, content_type, width, height, size_bytes, bytes, created_at)
				VALUES (:id, :contentType, 320, 400, :size, :bytes, now())""")
			.param("id", id)
			.param("contentType", contentType)
			.param("size", bytes.length)
			.param("bytes", bytes)
			.update();
		return id;
	}

	/** Takes the content offline, as a retirement does. */
	public void retire(UUID contentId) {
		this.owner.sql("UPDATE content_version SET status = 'RETIRED' WHERE content_id = :id AND status = 'PUBLISHED'")
			.param("id", contentId)
			.update();
	}

	/** A new version with another romaji goes online: the previous one is superseded. */
	public void republishWithRomaji(UUID contentId, String romaji, Translation... translations) {
		this.owner
			.sql("UPDATE content_version SET status = 'SUPERSEDED' WHERE content_id = :id AND status = 'PUBLISHED'")
			.param("id", contentId)
			.update();
		insertOnlineVersion(TYPE, contentId, 2, romaji, translations);
	}

	/** A saga online, its slug being its number. */
	public UUID publishSaga(int number, Translation... translations) {
		return story(SAGA, PUBLISHED, number, "INSERT INTO saga_version (version_id, number) VALUES (:id, :number)",
				spec -> spec, translations);
	}

	/** An arc online of an online saga. */
	public UUID publishArc(int number, UUID sagaId, Translation... translations) {
		return story(ARC, PUBLISHED, number,
				"INSERT INTO arc_version (version_id, number, saga_content_id) VALUES (:id, :number, :saga)",
				spec -> spec.param("saga", sagaId), translations);
	}

	/**
	 * A volume online, with its cover if any; only the titles of the translations count,
	 * a volume has no description.
	 */
	public UUID publishVolume(int number, String japaneseTitle, String coverImageId, Translation... translations) {
		return story(VOLUME, PUBLISHED, number, """
				INSERT INTO volume_version (version_id, number, japanese_title, cover_image_id)
				VALUES (:id, :number, :japaneseTitle, :cover)""",
				spec -> spec.param("japaneseTitle", japaneseTitle).param("cover", coverImageId), translations);
	}

	/** A chapter online of an online arc, in an online volume or in none. */
	public UUID publishChapter(int number, UUID arcId, UUID volumeId, String japaneseTitle, Integer pageCount,
			Translation... translations) {
		return story(CHAPTER, PUBLISHED, number, chapterSql(),
				spec -> chapterLinks(spec, arcId, volumeId, japaneseTitle, pageCount), translations);
	}

	/** A chapter still in draft: never online, nor counted by the span of its arc. */
	public UUID draftChapter(int number, UUID arcId, UUID volumeId, String japaneseTitle, Integer pageCount,
			Translation... translations) {
		return story(CHAPTER, "DRAFT", number, chapterSql(),
				spec -> chapterLinks(spec, arcId, volumeId, japaneseTitle, pageCount), translations);
	}

	/** An episode online, of an online arc or of none (a filler). */
	public UUID publishEpisode(int number, UUID arcId, String japaneseTitle, Translation... translations) {
		return story(EPISODE, PUBLISHED, number, """
				INSERT INTO episode_version (version_id, number, japanese_title, arc_content_id)
				VALUES (:id, :number, :japaneseTitle, :arc)""",
				spec -> spec.param("japaneseTitle", japaneseTitle).param("arc", arcId), translations);
	}

	/** The release day of an online volume in a country. */
	public void volumeRelease(UUID volumeId, String country, LocalDate date) {
		dayOf("volume_version_release", "release_date", volumeId, country, date);
	}

	/** The release day of an online chapter in a country. */
	public void chapterRelease(UUID chapterId, String country, LocalDate date) {
		dayOf("chapter_version_release", "release_date", chapterId, country, date);
	}

	/** The broadcast day of an online episode in a country. */
	public void episodeBroadcast(UUID episodeId, String country, LocalDate date) {
		dayOf("episode_version_broadcast", "broadcast_date", episodeId, country, date);
	}

	private void dayOf(String table, String dateColumn, UUID contentId, String country, LocalDate date) {
		this.owner
			.sql("INSERT INTO " + table + " (version_id, country, " + dateColumn + ") VALUES"
					+ " ((SELECT id FROM content_version WHERE content_id = :id AND status = 'PUBLISHED'),"
					+ " :country, :date)")
			.param("id", contentId)
			.param("country", country)
			.param("date", date)
			.update();
	}

	private static String chapterSql() {
		return """
				INSERT INTO chapter_version (version_id, number, japanese_title, page_count, arc_content_id,
				                             volume_content_id)
				VALUES (:id, :number, :japaneseTitle, :pageCount, :arc, :volume)""";
	}

	private static JdbcClient.StatementSpec chapterLinks(JdbcClient.StatementSpec spec, UUID arcId, UUID volumeId,
			String japaneseTitle, Integer pageCount) {
		return spec.param("arc", arcId)
			.param("volume", volumeId)
			.param("japaneseTitle", japaneseTitle)
			.param("pageCount", pageCount);
	}

	/**
	 * A bounty in the status of the poster, with its slug on record when online: the slug
	 * of its key text, as content-service records at publish. The description of each
	 * translation is the bounty's.
	 */
	public UUID bounty(Poster poster, Translation... translations) {
		UUID contentId = insertContent(BOUNTY);
		bountyVersion(contentId, 1, poster, translations);
		return contentId;
	}

	/**
	 * A new version of a bounty with another key goes online: the old one is superseded.
	 */
	public void republishBounty(UUID contentId, Poster poster, Translation... translations) {
		this.owner
			.sql("UPDATE content_version SET status = 'SUPERSEDED' WHERE content_id = :id AND status = 'PUBLISHED'")
			.param("id", contentId)
			.update();
		bountyVersion(contentId, 2, poster, translations);
	}

	private void bountyVersion(UUID contentId, int number, Poster poster, Translation... translations) {
		UUID versionId = insertContentVersion(contentId, number, poster.status());
		this.owner.sql("""
				INSERT INTO bounty_version (version_id, poster_name, amount, condition, inactive, previous_content_id,
				                            image_id, holder_content_id)
				VALUES (:id, :posterName, :amount, :condition, :inactive, :previous, :image, :holder)""")
			.param("id", versionId)
			.param("posterName", poster.posterName())
			.param("amount", poster.amount())
			.param("condition", poster.condition())
			.param("inactive", poster.inactive())
			.param("previous", poster.previousId())
			.param("image", poster.imageId())
			.param("holder", poster.holderId())
			.update();
		paragraphs(BOUNTY, versionId, translations);
		if (PUBLISHED.equals(poster.status())) {
			this.owner.sql("""
					INSERT INTO content_slug (entity_type, slug, content_id, assigned_at)
					SELECT :type, key_slug, :contentId, now() FROM bounty_version WHERE version_id = :id
					ON CONFLICT DO NOTHING""")
				.param("type", BOUNTY.type())
				.param("contentId", contentId)
				.param("id", versionId)
				.update();
		}
	}

	/**
	 * A story entity (keyed by a number, not a romaji) in the given status: its content,
	 * version, the row of its own columns, its translations and, when online, the slug
	 * content-service records at publish: the number itself.
	 */
	private UUID story(Entity entity, String status, int number, String insertSql,
			UnaryOperator<JdbcClient.StatementSpec> ownColumns, Translation... translations) {
		UUID contentId = insertContent(entity);
		UUID versionId = insertContentVersion(contentId, 1, status);
		ownColumns.apply(this.owner.sql(insertSql).param("id", versionId).param("number", number)).update();
		for (Translation translation : translations) {
			storyTranslation(entity, versionId, translation);
		}
		if (PARAGRAPHED_STORY.contains(entity)) {
			paragraphs(entity, versionId, translations);
		}
		if (PUBLISHED.equals(status)) {
			this.owner.sql("""
					INSERT INTO content_slug (entity_type, slug, content_id, assigned_at)
					VALUES (:type, slug_of(CAST(:number AS text)), :id, now())
					ON CONFLICT DO NOTHING""")
				.param("type", entity.type())
				.param("number", number)
				.param("id", contentId)
				.update();
		}
		return contentId;
	}

	/** An affiliation online, with its emblem if any and its slug on record. */
	public UUID publishAffiliation(String romaji, String imageId, Translation... translations) {
		UUID contentId = insertContent(AFFILIATION);
		affiliationVersion(contentId, 1, PUBLISHED, romaji, imageId, translations);
		return contentId;
	}

	/** An affiliation still in draft: never online, nor its emblem. */
	public void draftAffiliation(String romaji, String imageId, Translation... translations) {
		affiliationVersion(insertContent(AFFILIATION), 1, "DRAFT", romaji, imageId, translations);
	}

	/**
	 * A new version of an affiliation with another romaji goes online: the previous one
	 * is superseded, its slug kept on record.
	 */
	public void republishAffiliation(UUID contentId, String romaji, Translation... translations) {
		this.owner
			.sql("UPDATE content_version SET status = 'SUPERSEDED' WHERE content_id = :id AND status = 'PUBLISHED'")
			.param("id", contentId)
			.update();
		affiliationVersion(contentId, 2, PUBLISHED, romaji, null, translations);
	}

	private void affiliationVersion(UUID contentId, int number, String status, String romaji, String imageId,
			Translation... translations) {
		UUID versionId = insertContentVersion(contentId, number, status);
		this.owner.sql("INSERT INTO affiliation_version (version_id, romaji, image_id) VALUES (:id, :romaji, :image)")
			.param("id", versionId)
			.param("romaji", romaji)
			.param("image", imageId)
			.update();
		for (Translation translation : translations) {
			this.owner.sql("""
					INSERT INTO affiliation_version_translation (version_id, language_code, name)
					VALUES (:id, :language, :name)""")
				.param("id", versionId)
				.param("language", translation.language())
				.param("name", translation.name())
				.update();
		}
		paragraphs(AFFILIATION, versionId, translations);
		if (PUBLISHED.equals(status)) {
			recordSlug(AFFILIATION, contentId, romaji);
		}
	}

	/** Sets the kind of the version online of an affiliation. */
	public void affiliationKind(UUID contentId, String kind) {
		this.owner
			.sql("UPDATE affiliation_version SET kind = :kind WHERE version_id ="
					+ " (SELECT id FROM content_version WHERE content_id = :id AND status = 'PUBLISHED')")
			.param("kind", kind)
			.param("id", contentId)
			.update();
	}

	/** A species online, with its picture if any and its slug on record. */
	public UUID publishSpecies(String romaji, String imageId, Translation... translations) {
		UUID contentId = insertContent(SPECIES);
		speciesVersion(contentId, 1, PUBLISHED, romaji, imageId, translations);
		return contentId;
	}

	/** A species still in draft: never online, nor its picture. */
	public void draftSpecies(String romaji, String imageId, Translation... translations) {
		speciesVersion(insertContent(SPECIES), 1, "DRAFT", romaji, imageId, translations);
	}

	/**
	 * A new version of a species with another romaji goes online: the previous one is
	 * superseded, its slug kept on record.
	 */
	public void republishSpecies(UUID contentId, String romaji, Translation... translations) {
		this.owner
			.sql("UPDATE content_version SET status = 'SUPERSEDED' WHERE content_id = :id AND status = 'PUBLISHED'")
			.param("id", contentId)
			.update();
		speciesVersion(contentId, 2, PUBLISHED, romaji, null, translations);
	}

	private void speciesVersion(UUID contentId, int number, String status, String romaji, String imageId,
			Translation... translations) {
		UUID versionId = insertContentVersion(contentId, number, status);
		this.owner.sql("INSERT INTO species_version (version_id, romaji, image_id) VALUES (:id, :romaji, :image)")
			.param("id", versionId)
			.param("romaji", romaji)
			.param("image", imageId)
			.update();
		for (Translation translation : translations) {
			this.owner.sql("""
					INSERT INTO species_version_translation (version_id, language_code, name)
					VALUES (:id, :language, :name)""")
				.param("id", versionId)
				.param("language", translation.language())
				.param("name", translation.name())
				.update();
		}
		paragraphs(SPECIES, versionId, translations);
		if (PUBLISHED.equals(status)) {
			recordSlug(SPECIES, contentId, romaji);
		}
	}

	/** A character online, with its portrait if any and its slug on record. */
	public UUID publishCharacter(Persona persona, Translation... translations) {
		UUID contentId = insertContent(CHARACTER);
		characterVersion(contentId, 1, PUBLISHED, persona, translations);
		return contentId;
	}

	/** A character still in draft: never online, nor its portrait. */
	public void draftCharacter(Persona persona, Translation... translations) {
		characterVersion(insertContent(CHARACTER), 1, "DRAFT", persona, translations);
	}

	/**
	 * A new version of a character goes online: the previous one is superseded, its slug
	 * kept on record.
	 */
	public void republishCharacter(UUID contentId, Persona persona, Translation... translations) {
		this.owner
			.sql("UPDATE content_version SET status = 'SUPERSEDED' WHERE content_id = :id AND status = 'PUBLISHED'")
			.param("id", contentId)
			.update();
		characterVersion(contentId, 2, PUBLISHED, persona, translations);
	}

	private void characterVersion(UUID contentId, int number, String status, Persona persona,
			Translation... translations) {
		UUID versionId = insertContentVersion(contentId, number, status);
		MonthDay birthday = persona.birthday();
		this.owner.sql("""
				INSERT INTO character_version (version_id, romaji, japanese_name, status, gender, birthday_month,
				                               birthday_day)
				VALUES (:id, :romaji, :japaneseName, :status, :gender, :month, :day)""")
			.param("id", versionId)
			.param("romaji", persona.romaji())
			.param("japaneseName", persona.japaneseName())
			.param("status", persona.status())
			.param("gender", persona.gender())
			.param("month", birthday == null ? null : birthday.getMonthValue())
			.param("day", birthday == null ? null : birthday.getDayOfMonth())
			.update();
		if (persona.imageId() != null) {
			this.owner.sql("""
					INSERT INTO character_version_image (id, version_id, position, image_id, kind)
					VALUES (:id, :version, 0, :image, 'ASPETTO')""")
				.param("id", UUID.randomUUID())
				.param("version", versionId)
				.param("image", persona.imageId())
				.update();
		}
		for (Translation translation : translations) {
			this.owner.sql("""
					INSERT INTO character_version_translation (version_id, language_code, name)
					VALUES (:id, :language, :name)""")
				.param("id", versionId)
				.param("language", translation.language())
				.param("name", translation.name())
				.update();
		}
		paragraphs(CHARACTER, versionId, translations);
		characterLists(versionId, persona);
		devilFruitsOf(versionId, persona);
		relativesOf(versionId, persona);
		if (PUBLISHED.equals(status)) {
			recordSlug(CHARACTER, contentId, persona.romaji());
		}
	}

	/**
	 * The epithets, aliases, species, memberships and Devil Fruits of a character
	 * version, in the given order.
	 */
	private void characterLists(UUID versionId, Persona persona) {
		for (int position = 0; position < persona.epithets().size(); position++) {
			UUID epithetId = UUID.randomUUID();
			this.owner
				.sql("INSERT INTO character_version_epithet (id, version_id, position) VALUES (:id, :version, :at)")
				.param("id", epithetId)
				.param("version", versionId)
				.param("at", position)
				.update();
			persona.epithets()
				.get(position)
				.forEach((language, text) -> this.owner.sql("""
						INSERT INTO character_version_epithet_translation (epithet_id, language_code, text)
						VALUES (:id, :language, :text)""")
					.param("id", epithetId)
					.param("language", language)
					.param("text", text)
					.update());
		}
		for (int position = 0; position < persona.aliases().size(); position++) {
			this.owner
				.sql("INSERT INTO character_version_alias (version_id, position, alias) VALUES (:version, :at, :alias)")
				.param("version", versionId)
				.param("at", position)
				.param("alias", persona.aliases().get(position))
				.update();
		}
		for (int position = 0; position < persona.species().size(); position++) {
			this.owner.sql("""
					INSERT INTO character_version_species (version_id, position, species_content_id)
					VALUES (:version, :at, :species)""")
				.param("version", versionId)
				.param("at", position)
				.param("species", persona.species().get(position))
				.update();
		}
		for (int position = 0; position < persona.affiliations().size(); position++) {
			Belonging belonging = persona.affiliations().get(position);
			UUID membershipId = UUID.randomUUID();
			this.owner.sql("""
					INSERT INTO character_version_affiliation (id, version_id, position, affiliation_content_id)
					VALUES (:id, :version, :at, :affiliation)""")
				.param("id", membershipId)
				.param("version", versionId)
				.param("at", position)
				.param("affiliation", belonging.affiliationId())
				.update();
			for (int at = 0; at < belonging.periods().size(); at++) {
				period(membershipId, at, belonging.periods().get(at));
			}
		}
	}

	private void period(UUID membershipId, int position, Stay stay) {
		UUID periodId = UUID.randomUUID();
		this.owner.sql("""
				INSERT INTO character_version_affiliation_period (id, membership_id, position, marine_rank,
				                                                  ended_chapter_id)
				VALUES (:id, :membership, :at, :rank, :ended)""")
			.param("id", periodId)
			.param("membership", membershipId)
			.param("at", position)
			.param("rank", stay.marineRank())
			.param("ended", endedIf(stay.former()))
			.update();
		stay.role()
			.forEach((language, role) -> this.owner.sql("""
					INSERT INTO character_version_affiliation_period_translation (period_id, language_code, role)
					VALUES (:id, :language, :role)""")
				.param("id", periodId)
				.param("language", language)
				.param("role", role)
				.update());
	}

	private void devilFruitsOf(UUID versionId, Persona persona) {
		for (int position = 0; position < persona.devilFruits().size(); position++) {
			Having having = persona.devilFruits().get(position);
			this.owner.sql("""
					INSERT INTO character_version_devil_fruit (version_id, position, devil_fruit_content_id,
					                                           ended_chapter_id)
					VALUES (:version, :at, :fruit, :ended)""")
				.param("version", versionId)
				.param("at", position)
				.param("fruit", having.devilFruitId())
				.param("ended", endedIf(having.former()))
				.update();
		}
	}

	private void relativesOf(UUID versionId, Persona persona) {
		for (int position = 0; position < persona.relatives().size(); position++) {
			Related related = persona.relatives().get(position);
			this.owner.sql("""
					INSERT INTO character_version_relative (version_id, position, relative_content_id, kind)
					VALUES (:version, :at, :relative, :kind)""")
				.param("version", versionId)
				.param("at", position)
				.param("relative", related.relativeId())
				.param("kind", related.kind())
				.update();
		}
	}

	/**
	 * The window of an entry of a list of the character online (workflow 3.7.2), by its
	 * position in the list.
	 */
	public void window(UUID characterId, CharacterList list, int position, Window window) {
		this.owner.sql("UPDATE " + list.table() + " t SET " + WINDOW_COLUMNS + """
				 FROM content_version v
				WHERE t.version_id = v.id AND v.content_id = :id AND v.status = 'PUBLISHED' AND t.position = :at""")
			.params(window.parameters())
			.param("id", characterId)
			.param("at", position)
			.update();
	}

	/**
	 * The window of a period of a membership of the character online (workflow 3.4.7), by
	 * the positions of the membership and of the period.
	 */
	public void periodWindow(UUID characterId, int membershipPosition, int periodPosition, Window window) {
		this.owner.sql("UPDATE character_version_affiliation_period t SET " + WINDOW_COLUMNS + """
				 FROM character_version_affiliation a JOIN content_version v ON v.id = a.version_id
				WHERE t.membership_id = a.id AND v.content_id = :id AND v.status = 'PUBLISHED'
				  AND a.position = :membership AND t.position = :at""")
			.params(window.parameters())
			.param("id", characterId)
			.param("membership", membershipPosition)
			.param("at", periodPosition)
			.update();
	}

	/**
	 * An image of the gallery of the character online (workflow 3.5.1), discovered at a
	 * chapter online or nowhere, and ended when former; its caption by language.
	 */
	public void galleryImage(UUID characterId, String imageId, String kind, UUID discoveredChapterId, boolean former,
			Map<String, String> captions) {
		UUID imageRowId = UUID.randomUUID();
		this.owner.sql("""
				INSERT INTO character_version_image (id, version_id, position, image_id, kind, discovered_chapter_id,
				                                     ended_chapter_id)
				SELECT :rowId, v.id, (SELECT count(*) FROM character_version_image i WHERE i.version_id = v.id), :image,
				       :kind, :discovered, :ended
				FROM content_version v
				WHERE v.content_id = :id AND v.status = 'PUBLISHED'""")
			.param("rowId", imageRowId)
			.param("id", characterId)
			.param("image", imageId)
			.param("kind", kind)
			.param("discovered", discoveredChapterId)
			.param("ended", endedIf(former))
			.update();
		captions.forEach((language, caption) -> this.owner.sql("""
				INSERT INTO character_version_image_translation (gallery_image_id, language_code, caption)
				VALUES (:id, :language, :caption)""")
			.param("id", imageRowId)
			.param("language", language)
			.param("caption", caption)
			.update());
	}

	/**
	 * A paragraph of the description of the character online, after the others, that the
	 * reader learns no longer holds: its text superseded (workflow 3.7.3).
	 */
	public void formerParagraph(UUID characterId, String language, String text) {
		UUID paragraphId = UUID.randomUUID();
		this.owner
			.sql("""
					INSERT INTO character_version_paragraph (id, version_id, position, section, ended_chapter_id)
					SELECT :paragraphId, v.id, (SELECT count(*) FROM character_version_paragraph p WHERE p.version_id = v.id),
					       'DESCRIPTION', :ended
					FROM content_version v
					WHERE v.content_id = :id AND v.status = 'PUBLISHED'""")
			.param("paragraphId", paragraphId)
			.param("id", characterId)
			.param("ended", endedIf(true))
			.update();
		this.owner.sql("""
				INSERT INTO character_version_paragraph_translation (paragraph_id, language_code, text)
				VALUES (:id, :language, :text)""")
			.param("id", paragraphId)
			.param("language", language)
			.param("text", text)
			.update();
	}

	/**
	 * The window of a paragraph of the content online (workflow 3.7.3), by its position
	 * among all its paragraphs.
	 */
	public void paragraphWindow(UUID contentId, int position, Window window) {
		this.owner.sql("UPDATE " + paragraphTable(contentId) + " t SET " + WINDOW_COLUMNS + """
				 FROM content_version v
				WHERE t.version_id = v.id AND v.content_id = :id AND v.status = 'PUBLISHED' AND t.position = :at""")
			.params(window.parameters())
			.param("id", contentId)
			.param("at", position)
			.update();
	}

	/**
	 * One more paragraph of a section of the content online, in one language, inside its
	 * window: the last of its paragraphs.
	 * @param section {@code DESCRIPTION}, {@code ADVANTAGES} or {@code DISADVANTAGES}
	 */
	public void windowedParagraph(UUID contentId, String section, String language, String text, Window window) {
		String table = paragraphTable(contentId);
		UUID paragraphId = UUID.randomUUID();
		this.owner
			.sql("INSERT INTO " + table + " (id, version_id, position, section) SELECT :paragraphId, v.id,"
					+ " (SELECT count(*) FROM " + table + " p WHERE p.version_id = v.id), :section"
					+ " FROM content_version v WHERE v.content_id = :id AND v.status = 'PUBLISHED'")
			.param("paragraphId", paragraphId)
			.param("id", contentId)
			.param("section", section)
			.update();
		this.owner.sql("UPDATE " + table + " SET " + WINDOW_COLUMNS + " WHERE id = :paragraphId")
			.params(window.parameters())
			.param("paragraphId", paragraphId)
			.update();
		this.owner
			.sql("INSERT INTO " + table + "_translation (paragraph_id, language_code, text)"
					+ " VALUES (:id, :language, :text)")
			.param("id", paragraphId)
			.param("language", language)
			.param("text", text)
			.update();
	}

	/**
	 * The paragraph table of the entity of a content, such as
	 * {@code saga_version_paragraph}.
	 */
	private String paragraphTable(UUID contentId) {
		String entityType = this.owner.sql("SELECT entity_type FROM content WHERE id = :id")
			.param("id", contentId)
			.query(String.class)
			.single();
		return entityType.toLowerCase(Locale.ROOT) + "_version_paragraph";
	}

	/** What the reader believes of the character online at its first appearance. */
	public void characterInitialKnowledge(UUID characterId, String status) {
		this.owner.sql("""
				INSERT INTO character_version_initial_knowledge (version_id, status)
				SELECT id, :status FROM content_version WHERE content_id = :id AND status = 'PUBLISHED'""")
			.param("id", characterId)
			.param("status", status)
			.update();
	}

	/**
	 * What the reader believes of the fruit online at its first appearance: its romaji,
	 * its type and its name by language.
	 */
	public void devilFruitInitialKnowledge(UUID fruitId, String romaji, UUID typeId, Map<String, String> names) {
		this.owner.sql("""
				INSERT INTO devil_fruit_version_initial_knowledge (version_id, romaji, type_content_id)
				SELECT id, :romaji, :type FROM content_version WHERE content_id = :id AND status = 'PUBLISHED'""")
			.param("id", fruitId)
			.param("romaji", romaji)
			.param("type", typeId)
			.update();
		names.forEach((language, name) -> this.owner.sql("""
				INSERT INTO devil_fruit_version_initial_knowledge_translation (version_id, language_code, name)
				SELECT id, :language, :name FROM content_version WHERE content_id = :id AND status = 'PUBLISHED'""")
			.param("id", fruitId)
			.param("language", language)
			.param("name", name)
			.update());
	}

	/** What the reader believes of the bounty online at its first appearance. */
	public void bountyInitialKnowledge(UUID bountyId, boolean inactive) {
		this.owner.sql("""
				INSERT INTO bounty_version_initial_knowledge (version_id, inactive)
				SELECT id, :inactive FROM content_version WHERE content_id = :id AND status = 'PUBLISHED'""")
			.param("id", bountyId)
			.param("inactive", inactive)
			.update();
	}

	/**
	 * A reveal of the character online at a chapter online: the fields named, no value of
	 * its own, so it reveals the current one; its English title.
	 */
	public void characterReveal(UUID characterId, UUID chapterId, String title, String... fields) {
		reveal(CHARACTER, characterId, chapterId, title, fields);
	}

	/** A reveal of the fruit online, as {@link #characterReveal}. */
	public void devilFruitReveal(UUID fruitId, UUID chapterId, String title, String... fields) {
		reveal(FRUIT, fruitId, chapterId, title, fields);
	}

	/** A reveal of the bounty online, as {@link #characterReveal}. */
	public void bountyReveal(UUID bountyId, UUID chapterId, String title, String... fields) {
		reveal(BOUNTY, bountyId, chapterId, title, fields);
	}

	private void reveal(Entity entity, UUID contentId, UUID chapterId, String title, String... fields) {
		UUID revealId = UUID.randomUUID();
		String table = entity.table() + "_reveal";
		this.owner
			.sql("INSERT INTO " + table + " (id, version_id, position, revealed_chapter_id)"
					+ " SELECT :revealId, v.id, (SELECT count(*) FROM " + table
					+ " r WHERE r.version_id = v.id), :chapter"
					+ " FROM content_version v WHERE v.content_id = :id AND v.status = 'PUBLISHED'")
			.param("revealId", revealId)
			.param("id", contentId)
			.param("chapter", chapterId)
			.update();
		for (String field : fields) {
			this.owner.sql("INSERT INTO " + table + "_field (reveal_id, field) VALUES (:id, :field)")
				.param("id", revealId)
				.param("field", field)
				.update();
		}
		this.owner
			.sql("INSERT INTO " + table + "_translation (reveal_id, language_code, title) VALUES (:id, 'en', :title)")
			.param("id", revealId)
			.param("title", title)
			.update();
	}

	/** Whether several characters may have the power of the fruit online at once. */
	public void multipleUsers(UUID fruitId) {
		this.owner
			.sql("UPDATE devil_fruit_version SET multiple_users = true WHERE version_id ="
					+ " (SELECT id FROM content_version WHERE content_id = :id AND status = 'PUBLISHED')")
			.param("id", fruitId)
			.update();
	}

	private void storyTranslation(Entity entity, UUID versionId, Translation translation) {
		boolean described = !VOLUME.equals(entity) && !PARAGRAPHED_STORY.contains(entity);
		this.owner
			.sql("INSERT INTO " + entity.table() + "_translation (version_id, language_code, title"
					+ (described ? ", description" : "") + ") VALUES (:id, :language, :title"
					+ (described ? ", :description" : "") + ")")
			.param("id", versionId)
			.param("language", translation.language())
			.param("title", translation.name())
			.param("description", translation.description())
			.update();
	}

	private UUID insertContent(Entity entity) {
		UUID contentId = UUID.randomUUID();
		this.owner.sql("INSERT INTO content (id, entity_type, created_at) VALUES (:id, :type, now())")
			.param("id", contentId)
			.param("type", entity.type())
			.update();
		return contentId;
	}

	private void fruitLinks(UUID versionId, UUID typeId, String imageId) {
		this.owner
			.sql("UPDATE devil_fruit_version SET type_content_id = :type, image_id = :image WHERE version_id = :id")
			.param("type", typeId)
			.param("image", imageId)
			.param("id", versionId)
			.update();
	}

	private UUID insertOnlineVersion(Entity entity, UUID contentId, int number, String romaji,
			Translation... translations) {
		UUID versionId = insertVersion(entity, contentId, number, "PUBLISHED", romaji, translations);
		recordSlug(entity, contentId, romaji);
		return versionId;
	}

	/** What content-service writes at publish: the slug of the romaji, kept for good. */
	private void recordSlug(Entity entity, UUID contentId, String romaji) {
		this.owner.sql("""
				INSERT INTO content_slug (entity_type, slug, content_id, assigned_at)
				VALUES (:type, slug_of(:romaji), :id, now())
				ON CONFLICT DO NOTHING""")
			.param("type", entity.type())
			.param("romaji", romaji)
			.param("id", contentId)
			.update();
	}

	private UUID insertVersion(Entity entity, UUID contentId, int number, String status, String romaji,
			Translation... translations) {
		UUID versionId = insertContentVersion(contentId, number, status);
		this.owner.sql("INSERT INTO " + entity.table() + " (version_id, romaji) VALUES (:id, :romaji)")
			.param("id", versionId)
			.param("romaji", romaji)
			.update();
		for (Translation translation : translations) {
			insertTranslation(entity, versionId, translation);
		}
		paragraphs(entity, versionId, translations);
		return versionId;
	}

	/** The row every version has, whatever its entity; returns its id. */
	private UUID insertContentVersion(UUID contentId, int number, String status) {
		UUID versionId = UUID.randomUUID();
		this.owner.sql("""
				INSERT INTO content_version (id, content_id, version_number, author_user_id, author_username,
				                             author_email, status, created_at, updated_at)
				VALUES (:id, :contentId, :number, :authorId, :author, 'fixture@example.org', :status,
				        '2026-10-02T14:31:07.123456Z', '2026-10-02T14:31:07.123456Z')""")
			.param("id", versionId)
			.param("contentId", contentId)
			.param("number", number)
			.param("status", status)
			.param("authorId", UUID.randomUUID())
			.param("author", AUTHOR)
			.update();
		return versionId;
	}

	private void insertTranslation(Entity entity, UUID versionId, Translation translation) {
		this.owner
			.sql("INSERT INTO " + entity.table() + "_translation (version_id, language_code, name)"
					+ " VALUES (:id, :language, :name)")
			.param("id", versionId)
			.param("language", translation.language())
			.param("name", translation.name())
			.update();
	}

	/**
	 * The long texts of a version as content-service stores them: one paragraph per
	 * section written in some language, open on the reader axis, its text per language.
	 */
	private void paragraphs(Entity entity, UUID versionId, Translation... translations) {
		int position = 0;
		for (Section section : Section.values()) {
			Map<String, String> texts = new LinkedHashMap<>();
			for (Translation translation : translations) {
				String text = section.of(translation);
				if (text != null) {
					texts.put(translation.language(), text);
				}
			}
			if (!texts.isEmpty()) {
				paragraph(entity, versionId, position++, section, texts);
			}
		}
	}

	private void paragraph(Entity entity, UUID versionId, int position, Section section, Map<String, String> texts) {
		UUID paragraphId = UUID.randomUUID();
		this.owner
			.sql("INSERT INTO " + entity.table() + "_paragraph (id, version_id, position, section)"
					+ " VALUES (:id, :version, :at, :section)")
			.param("id", paragraphId)
			.param("version", versionId)
			.param("at", position)
			.param("section", section.name())
			.update();
		texts.forEach((language, text) -> this.owner
			.sql("INSERT INTO " + entity.table() + "_paragraph_translation (paragraph_id, language_code, text)"
					+ " VALUES (:id, :language, :text)")
			.param("id", paragraphId)
			.param("language", language)
			.param("text", text)
			.update());
	}

	/**
	 * A chapter online that ends a former membership or fruit use, in no arc nor volume:
	 * the window ends at its number (workflow 3.7.2). Null for a current one.
	 */
	private UUID endedIf(boolean former) {
		return former ? story(CHAPTER, PUBLISHED, ENDING_CHAPTER, chapterSql(),
				(spec) -> chapterLinks(spec, null, null, null, null)) : null;
	}

	/** The sections of the long texts, in the order of a version. */
	private enum Section {

		DESCRIPTION(Translation::description), ADVANTAGES(Translation::advantages),
		DISADVANTAGES(Translation::disadvantages);

		private final Function<Translation, String> text;

		Section(Function<Translation, String> text) {
			this.text = text;
		}

		String of(Translation translation) {
			return this.text.apply(translation);
		}

	}

	/**
	 * An entity as stored: its type and the table of its versions, next to their
	 * {@code _translation}. Constants of this class only.
	 */
	private record Entity(String type, String table) {

	}

	/**
	 * What a bounty version holds besides its texts: online and dead or alive unless told
	 * otherwise.
	 */
	@Builder
	public record Poster(String posterName, long amount, String condition, boolean inactive, UUID previousId,
			String imageId, UUID holderId, String status) {

		public Poster {
			condition = condition == null ? "DEAD_OR_ALIVE" : condition;
			status = status == null ? PUBLISHED : status;
		}

	}

	/**
	 * What a character version holds besides its name and description: alive and male,
	 * with no lists, unless told otherwise.
	 *
	 * @param epithets each one's text by language
	 */
	@Builder
	public record Persona(String romaji, String japaneseName, String status, String gender, MonthDay birthday,
			String imageId, List<Map<String, String>> epithets, List<String> aliases, List<UUID> species,
			List<Belonging> affiliations, List<Having> devilFruits, List<Related> relatives) {

		public Persona {
			status = status == null ? "ALIVE" : status;
			gender = gender == null ? "MALE" : gender;
			epithets = epithets == null ? List.of() : epithets;
			aliases = aliases == null ? List.of() : aliases;
			species = species == null ? List.of() : species;
			affiliations = affiliations == null ? List.of() : affiliations;
			devilFruits = devilFruits == null ? List.of() : devilFruits;
			relatives = relatives == null ? List.of() : relatives;
		}

	}

	/** The lists of a character with one window per entry (workflow 3.7.2). */
	public enum CharacterList {

		EPITHET("character_version_epithet"), ALIAS("character_version_alias"), SPECIES("character_version_species"),
		DEVIL_FRUIT("character_version_devil_fruit"), RELATIVE("character_version_relative");

		private final String table;

		CharacterList(String table) {
			this.table = table;
		}

		String table() {
			return this.table;
		}

	}

	/**
	 * A window as chapters and episodes online (workflow 3.7.2); a null part is missing.
	 */
	@Builder
	public record Window(UUID discoveredChapter, UUID discoveredEpisode, UUID endedChapter, UUID endedEpisode) {

		Map<String, Object> parameters() {
			Map<String, Object> parameters = new LinkedHashMap<>();
			parameters.put("discoveredChapter", this.discoveredChapter);
			parameters.put("discoveredEpisode", this.discoveredEpisode);
			parameters.put("endedChapter", this.endedChapter);
			parameters.put("endedEpisode", this.endedEpisode);
			return parameters;
		}

	}

	/** A relative of a character version: the character and what it is to this one. */
	public record Related(UUID relativeId, String kind) {

	}

	/** A Devil Fruit of a character version: the fruit and whether it is a past one. */
	public record Having(UUID devilFruitId, boolean former) {

	}

	/**
	 * A membership of a character version: the affiliation and its periods, in order
	 * (workflow 3.4.7).
	 */
	public record Belonging(UUID affiliationId, List<Stay> periods) {

		/** A membership of one period. */
		public Belonging(UUID affiliationId, Map<String, String> role, String marineRank, boolean former) {
			this(affiliationId, List.of(new Stay(role, marineRank, former)));
		}

	}

	/**
	 * A period of a membership: its role by language, its rank and whether it ended.
	 */
	public record Stay(Map<String, String> role, String marineRank, boolean former) {

		public Stay {
			role = role == null ? Map.of() : role;
		}

	}

	public record Translation(String language, String name, String description, String advantages,
			String disadvantages) {

		public static Translation of(String language, String name) {
			return new Translation(language, name, "Description of " + name, null, null);
		}

	}

}
