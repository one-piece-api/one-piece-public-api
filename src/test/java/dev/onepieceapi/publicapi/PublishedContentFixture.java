package dev.onepieceapi.publicapi;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * The one place that writes test data into content-service's internal tables, as their
 * owner (the reader role cannot, by design). The public API's tests are the consumer side
 * of the view contract; this is the only code that knows what is behind the views.
 */
public class PublishedContentFixture {

	private static final String AUTHOR = "fixture-author";

	private static final Entity TYPE = new Entity("DEVIL_FRUIT_TYPE", "devil_fruit_type_version");

	private static final Entity FRUIT = new Entity("DEVIL_FRUIT", "devil_fruit_version");

	private static final Entity SAGA = new Entity("SAGA", "saga_version");

	private static final Entity ARC = new Entity("ARC", "arc_version");

	private static final Entity VOLUME = new Entity("VOLUME", "volume_version");

	private static final Entity CHAPTER = new Entity("CHAPTER", "chapter_version");

	private static final Entity EPISODE = new Entity("EPISODE", "episode_version");

	private static final String PUBLISHED = "PUBLISHED";

	private final JdbcClient owner;

	public PublishedContentFixture(PostgreSQLContainer postgres) {
		this.owner = JdbcClient
			.create(new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
	}

	/**
	 * Removes every content and image, leaving the language catalog as migrated. The
	 * fruits go first: a type with fruits linked cannot be deleted, nor an image in use;
	 * likewise the chapters and episodes before their arcs and volumes, and those before
	 * the sagas.
	 */
	public void clear() {
		this.owner.sql("DELETE FROM content WHERE entity_type = :type").param("type", FRUIT.type()).update();
		for (Entity entity : new Entity[] { CHAPTER, EPISODE, ARC, VOLUME, SAGA }) {
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

	/** A Devil Fruit still in draft: never online, nor its image. */
	public void draftFruit(String romaji, UUID typeId, String imageId, Translation... translations) {
		UUID versionId = insertVersion(FRUIT, insertContent(FRUIT), 1, "DRAFT", romaji, translations);
		fruitLinks(versionId, typeId, imageId);
	}

	/** A stored image whose id is the given hex digit repeated; returns the id. */
	public String image(char hexDigit, byte[] bytes) {
		String id = String.valueOf(hexDigit).repeat(64);
		this.owner.sql("""
				INSERT INTO image (id, content_type, width, height, size_bytes, bytes, created_at)
				VALUES (:id, 'image/png', 320, 400, :size, :bytes, now())""")
			.param("id", id)
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
	 * A story entity (keyed by a number, not a romaji) in the given status: its content,
	 * version, the row of its own columns, its translations and, when online, the slug
	 * content-service records at publish: the number itself.
	 */
	private UUID story(Entity entity, String status, int number, String insertSql,
			UnaryOperator<JdbcClient.StatementSpec> ownColumns, Translation... translations) {
		UUID contentId = insertContent(entity);
		UUID versionId = UUID.randomUUID();
		this.owner.sql("""
				INSERT INTO content_version (id, content_id, version_number, author_user_id, author_username,
				                             author_email, status, created_at, updated_at)
				VALUES (:id, :contentId, 1, :authorId, :author, 'fixture@example.org', :status,
				        '2026-10-02T14:31:07.123456Z', '2026-10-02T14:31:07.123456Z')""")
			.param("id", versionId)
			.param("contentId", contentId)
			.param("status", status)
			.param("authorId", UUID.randomUUID())
			.param("author", AUTHOR)
			.update();
		ownColumns.apply(this.owner.sql(insertSql).param("id", versionId).param("number", number)).update();
		for (Translation translation : translations) {
			storyTranslation(entity, versionId, translation);
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

	private void storyTranslation(Entity entity, UUID versionId, Translation translation) {
		boolean described = !VOLUME.equals(entity);
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
		// What content-service writes at publish: the slug of the romaji, kept for good.
		this.owner.sql("""
				INSERT INTO content_slug (entity_type, slug, content_id, assigned_at)
				VALUES (:type, slug_of(:romaji), :id, now())
				ON CONFLICT DO NOTHING""")
			.param("type", entity.type())
			.param("romaji", romaji)
			.param("id", contentId)
			.update();
		return versionId;
	}

	private UUID insertVersion(Entity entity, UUID contentId, int number, String status, String romaji,
			Translation... translations) {
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
		this.owner.sql("INSERT INTO " + entity.table() + " (version_id, romaji) VALUES (:id, :romaji)")
			.param("id", versionId)
			.param("romaji", romaji)
			.update();
		for (Translation translation : translations) {
			insertTranslation(entity, versionId, translation);
		}
		return versionId;
	}

	private void insertTranslation(Entity entity, UUID versionId, Translation translation) {
		this.owner
			.sql("INSERT INTO " + entity.table() + "_translation"
					+ " (version_id, language_code, name, description, advantages, disadvantages)"
					+ " VALUES (:id, :language, :name, :description, :advantages, :disadvantages)")
			.param("id", versionId)
			.param("language", translation.language())
			.param("name", translation.name())
			.param("description", translation.description())
			.param("advantages", translation.advantages())
			.param("disadvantages", translation.disadvantages())
			.update();
	}

	/**
	 * An entity as stored: its type and the table of its versions, next to their
	 * {@code _translation}. Constants of this class only.
	 */
	private record Entity(String type, String table) {

	}

	public record Translation(String language, String name, String description, String advantages,
			String disadvantages) {

		public static Translation of(String language, String name) {
			return new Translation(language, name, "Description of " + name, null, null);
		}

	}

}
