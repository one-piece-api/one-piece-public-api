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

	private final JdbcClient owner;

	public PublishedContentFixture(PostgreSQLContainer postgres) {
		this.owner = JdbcClient
			.create(new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
	}

	/** Removes every content, leaving the language catalog as migrated. */
	public void clear() {
		this.owner.sql("DELETE FROM content").update();
	}

	/** A Devil Fruit Type online, with its slug on record. */
	public UUID publish(String romaji, Translation... translations) {
		UUID contentId = UUID.randomUUID();
		this.owner.sql("INSERT INTO content (id, entity_type, created_at) VALUES (:id, 'DEVIL_FRUIT_TYPE', now())")
			.param("id", contentId)
			.update();
		insertOnlineVersion(contentId, 1, romaji, translations);
		return contentId;
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
		insertOnlineVersion(contentId, 2, romaji, translations);
	}

	private void insertOnlineVersion(UUID contentId, int number, String romaji, Translation... translations) {
		UUID versionId = UUID.randomUUID();
		this.owner.sql("""
				INSERT INTO content_version (id, content_id, version_number, author_user_id, author_username,
				                             author_email, status, created_at, updated_at)
				VALUES (:id, :contentId, :number, :authorId, :author, 'fixture@example.org', 'PUBLISHED',
				        '2026-10-02T14:31:07.123456Z', '2026-10-02T14:31:07.123456Z')""")
			.param("id", versionId)
			.param("contentId", contentId)
			.param("number", number)
			.param("authorId", UUID.randomUUID())
			.param("author", AUTHOR)
			.update();
		this.owner.sql("INSERT INTO devil_fruit_type_version (version_id, romaji) VALUES (:id, :romaji)")
			.param("id", versionId)
			.param("romaji", romaji)
			.update();
		for (Translation translation : translations) {
			insertTranslation(versionId, translation);
		}
		// What content-service writes at publish: the slug of the romaji, kept for good.
		this.owner.sql("""
				INSERT INTO content_slug (entity_type, slug, content_id, assigned_at)
				VALUES ('DEVIL_FRUIT_TYPE', slug_of(:romaji), :id, now())
				ON CONFLICT DO NOTHING""").param("romaji", romaji).param("id", contentId).update();
	}

	private void insertTranslation(UUID versionId, Translation translation) {
		this.owner.sql("""
				INSERT INTO devil_fruit_type_version_translation (version_id, language_code, name, description,
				                                                  advantages, disadvantages)
				VALUES (:id, :language, :name, :description, :advantages, :disadvantages)""")
			.param("id", versionId)
			.param("language", translation.language())
			.param("name", translation.name())
			.param("description", translation.description())
			.param("advantages", translation.advantages())
			.param("disadvantages", translation.disadvantages())
			.update();
	}

	public record Translation(String language, String name, String description, String advantages,
			String disadvantages) {

		public static Translation of(String language, String name) {
			return new Translation(language, name, "Description of " + name, null, null);
		}

	}

}
