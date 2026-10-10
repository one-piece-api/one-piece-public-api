package dev.onepieceapi.publicapi;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

/**
 * The one place that writes test data into content-service's internal tables, as their
 * owner (the reader role cannot, by design). The public API's tests are the consumer side
 * of the view contract; this is the only code that knows what is behind the views.
 */
public class PublishedContentFixture {

	private static final String AUTHOR = "fixture-author";

	private static final Entity TYPE = new Entity("DEVIL_FRUIT_TYPE", "devil_fruit_type_version");

	private static final Entity FRUIT = new Entity("DEVIL_FRUIT", "devil_fruit_version");

	private final JdbcClient owner;

	public PublishedContentFixture(PostgreSQLContainer postgres) {
		this.owner = JdbcClient
			.create(new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
	}

	/**
	 * Removes every content and image, leaving the language catalog as migrated. The
	 * fruits go first: a type with fruits linked cannot be deleted, nor an image in use.
	 */
	public void clear() {
		this.owner.sql("DELETE FROM content WHERE entity_type = :type").param("type", FRUIT.type()).update();
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
