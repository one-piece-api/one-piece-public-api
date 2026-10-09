package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.ContentAddress;
import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
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

	private static final String FILTER_PARAMETER = "filter";

	private final JdbcClient jdbc;

	public <D, S> Page<S> search(PublishedView<D, S> view, String language, ContentSearch search) {
		Map<String, Object> parameters = new HashMap<>(
				Map.of("language", language, "pattern", containsPattern(search.text())));
		StringBuilder where = new StringBuilder(LANGUAGE_FILTER);
		if (search.filtered()) {
			where.append(TEXT_FILTER);
		}
		appendFilters(view, search.filters(), where, parameters);
		var content = this.jdbc
			.sql("SELECT " + view.summaryColumns() + " FROM " + view.view() + " WHERE " + where + " ORDER BY "
					+ orderBy(search.sort()) + ", id LIMIT :limit OFFSET :offset")
			.params(parameters)
			.param("limit", search.size())
			.param("offset", search.offset())
			.query(view.summary())
			.list();
		long total = this.jdbc.sql("SELECT count(*) FROM " + view.view() + " WHERE " + where)
			.params(parameters)
			.query(Long.class)
			.single();
		return new Page<>(content, search.page(), search.size(), total);
	}

	/**
	 * Every row matching one filter, by name then id: for a relation embedded in another
	 * content, bounded by nature and so not paginated (plan D12).
	 */
	public <D, S> List<S> listAll(PublishedView<D, S> view, String language, ContentFilterValue filter) {
		Map<String, Object> parameters = new HashMap<>(Map.of("language", language));
		StringBuilder where = new StringBuilder(LANGUAGE_FILTER);
		appendFilters(view, List.of(filter), where, parameters);
		return this.jdbc
			.sql("SELECT " + view.summaryColumns() + " FROM " + view.view() + " WHERE " + where + " ORDER BY name, id")
			.params(parameters)
			.query(view.summary())
			.list();
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

	/**
	 * Each filter's fixed condition, its value bound under a parameter of its own. A
	 * filter the entity does not declare is a programming error: the controllers only
	 * pass their own.
	 */
	private static void appendFilters(PublishedView<?, ?> view, List<ContentFilterValue> filters, StringBuilder where,
			Map<String, Object> parameters) {
		for (int index = 0; index < filters.size(); index++) {
			ContentFilterValue filter = filters.get(index);
			ContentFilter declared = view.filters().get(filter.name());
			if (declared == null) {
				throw new IllegalArgumentException("No filter " + filter.name() + " on " + view.view());
			}
			String parameter = FILTER_PARAMETER + index;
			boolean byId = ContentAddress.isId(filter.idOrSlug());
			where.append(" AND ").append((byId ? declared.byId() : declared.bySlug()).formatted(parameter));
			parameters.put(parameter, byId ? UUID.fromString(filter.idOrSlug()) : filter.idOrSlug());
		}
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
