package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The contents online of any entity, read through the {@code published} view its
 * {@link PublishedView} names. The SQL is composed only from the fixed fragments of the
 * descriptor and of this class; every value a caller sends is a bind parameter (plan D3).
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedContentRepository {

	private static final String LANGUAGE_FILTER = "language = :language";

	/**
	 * Left out when nobody searches: comparing every row with an empty pattern is pure
	 * cost.
	 */
	private static final String TEXT_FILTER = " AND (name ILIKE :pattern OR romaji ILIKE :pattern)";

	private final JdbcClient jdbc;

	public <D, S> Page<S> search(PublishedView<D, S> view, String language, ContentSearch search) {
		Map<String, Object> parameters = Map.of("language", language, "pattern", containsPattern(search.text()));
		String filter = search.filtered() ? LANGUAGE_FILTER + TEXT_FILTER : LANGUAGE_FILTER;
		var content = this.jdbc
			.sql("SELECT " + view.summaryColumns() + " FROM " + view.view() + " WHERE " + filter + " ORDER BY "
					+ orderBy(search.sort()) + ", id LIMIT :limit OFFSET :offset")
			.params(parameters)
			.param("limit", search.size())
			.param("offset", search.offset())
			.query(view.summary())
			.list();
		long total = this.jdbc.sql("SELECT count(*) FROM " + view.view() + " WHERE " + filter)
			.params(parameters)
			.query(Long.class)
			.single();
		return new Page<>(content, search.page(), search.size(), total);
	}

	public <D, S> Optional<D> findById(PublishedView<D, S> view, String language, UUID id) {
		return this.jdbc
			.sql("SELECT " + view.detailColumns() + " FROM " + view.view() + " WHERE language = :language AND id = :id")
			.param("language", language)
			.param("id", id)
			.query(view.detail())
			.optional();
	}

	/** By a slug the content has had online: the current one or an old one (plan D5). */
	public <D, S> Optional<D> findBySlug(PublishedView<D, S> view, String language, String slug) {
		return this.jdbc
			.sql("SELECT " + view.detailColumns() + " FROM " + view.view() + " WHERE language = :language"
					+ " AND id = (SELECT content_id FROM published.content_slug"
					+ " WHERE entity_type = :entityType AND slug = :slug)")
			.param("language", language)
			.param("entityType", view.entityType())
			.param("slug", slug)
			.query(view.detail())
			.optional();
	}

	private static String orderBy(ContentSort sort) {
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
