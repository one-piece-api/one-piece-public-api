package dev.onepieceapi.publicapi.persistence;

import lombok.Builder;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.util.Map;
import java.util.function.Function;

/**
 * What the generic repository needs to read one entity: data only, no behaviour. The
 * strings are fixed SQL fragments written next to the entity; nothing a caller sends ever
 * becomes one (plan D3).
 *
 * @param entityType the entity type of {@code published.content_slug}
 * @param view the view of the entity, with its schema
 * @param detailColumns the columns of the detail
 * @param summaryColumns the columns of a list row
 * @param detail reads a detail row
 * @param summary reads a list row
 * @param filters the filters of the list, by name; none when null
 * @param <D> the detail
 * @param <S> the list row
 */
@Builder
public record PublishedView<D, S>(String entityType, String view, String detailColumns, String summaryColumns,
		RowMapper<D> detail, RowMapper<S> summary, Map<String, ContentFilter> filters) {

	public PublishedView {
		filters = filters == null ? Map.of() : Map.copyOf(filters);
	}

	/**
	 * A reader of rows shaped as {@code rowType}, turned into the domain by
	 * {@code toDomain}.
	 */
	public static <R, T> RowMapper<T> reading(Class<R> rowType, Function<R, T> toDomain) {
		var rows = new DataClassRowMapper<>(rowType);
		return (resultSet, rowNumber) -> toDomain.apply(rows.mapRow(resultSet, rowNumber));
	}

}
