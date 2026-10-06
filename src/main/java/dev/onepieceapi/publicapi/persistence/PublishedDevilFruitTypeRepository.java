package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSearch;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSort;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.persistence.mapper.PublishedRowMapper;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeSummaryRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The Devil Fruit Types online, read through {@code published.devil_fruit_type}. The SQL
 * is composed only from the fixed fragments below; every value a caller sends is a bind
 * parameter (plan D3).
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedDevilFruitTypeRepository {

	private static final String ENTITY_TYPE = "DEVIL_FRUIT_TYPE";

	private static final String DETAIL_COLUMNS = """
			id, slug, romaji, language, name, description, advantages, disadvantages, published_at""";

	private static final String LANGUAGE_FILTER = "language = :language";

	/**
	 * Left out when nobody searches: comparing every row with an empty pattern is pure
	 * cost.
	 */
	private static final String TEXT_FILTER = " AND (name ILIKE :pattern OR romaji ILIKE :pattern)";

	private final JdbcClient jdbc;

	public Page<DevilFruitTypeSummary> search(String language, DevilFruitTypeSearch search) {
		Map<String, Object> parameters = Map.of("language", language, "pattern", containsPattern(search.text()));
		String filter = search.filtered() ? LANGUAGE_FILTER + TEXT_FILTER : LANGUAGE_FILTER;
		var content = this.jdbc
			.sql("SELECT id, slug, romaji, name FROM published.devil_fruit_type WHERE " + filter + " ORDER BY "
					+ orderBy(search.sort()) + ", id LIMIT :limit OFFSET :offset")
			.params(parameters)
			.param("limit", search.size())
			.param("offset", search.offset())
			.query(DevilFruitTypeSummaryRow.class)
			.list()
			.stream()
			.map(PublishedRowMapper::toDomain)
			.toList();
		long total = this.jdbc.sql("SELECT count(*) FROM published.devil_fruit_type WHERE " + filter)
			.params(parameters)
			.query(Long.class)
			.single();
		return new Page<>(content, search.page(), search.size(), total);
	}

	public Optional<DevilFruitType> findById(String language, UUID id) {
		return this.jdbc
			.sql("SELECT " + DETAIL_COLUMNS
					+ " FROM published.devil_fruit_type WHERE language = :language AND id = :id")
			.param("language", language)
			.param("id", id)
			.query(DevilFruitTypeRow.class)
			.optional()
			.map(PublishedRowMapper::toDomain);
	}

	/** By a slug the content has had online: the current one or an old one (plan D5). */
	public Optional<DevilFruitType> findBySlug(String language, String slug) {
		return this.jdbc.sql("SELECT " + DETAIL_COLUMNS + """
				 FROM published.devil_fruit_type
				WHERE language = :language
				  AND id = (SELECT content_id FROM published.content_slug
				             WHERE entity_type = :entityType AND slug = :slug)""")
			.param("language", language)
			.param("entityType", ENTITY_TYPE)
			.param("slug", slug)
			.query(DevilFruitTypeRow.class)
			.optional()
			.map(PublishedRowMapper::toDomain);
	}

	private static String orderBy(DevilFruitTypeSort sort) {
		String column = switch (sort.field()) {
			case NAME -> "name";
			case ROMAJI -> "romaji";
			case PUBLISHED_AT -> "published_at";
		};
		return column + " " + sort.direction().name();
	}

	/**
	 * A "contains" pattern for {@code ILIKE}, with the characters it treats as wildcards
	 * escaped.
	 */
	private static String containsPattern(String text) {
		String literal = text == null ? "" : text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return "%" + literal + "%";
	}

}
