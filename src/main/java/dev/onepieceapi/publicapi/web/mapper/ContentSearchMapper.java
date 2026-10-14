package dev.onepieceapi.publicapi.web.mapper;

import dev.onepieceapi.publicapi.config.PublicApiProperties.Pagination;
import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.web.dto.request.ContentListRequest;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** From the query parameters of the list to what the service searches for. */
@UtilityClass
public class ContentSearchMapper {

	/**
	 * The filters a caller asked for: those with a value that is not blank. A filter the
	 * caller left out is not one.
	 */
	public List<ContentFilterValue> toFilters(Map<String, Optional<String>> asked) {
		return asked.entrySet()
			.stream()
			.filter(entry -> entry.getValue().filter(value -> !value.isBlank()).isPresent())
			.map(entry -> new ContentFilterValue(entry.getKey(), entry.getValue().get().strip()))
			.toList();
	}

	/** Applies the defaults and the page size cap (plan D8). */
	public ContentSearch toSearch(ContentListRequest request, Pagination pagination) {
		return toSearch(request, pagination, List.of());
	}

	/** As above, narrowed by the filters of the entity its controller read (plan D14). */
	public ContentSearch toSearch(ContentListRequest request, Pagination pagination, List<ContentFilterValue> filters) {
		return toSearch(request, pagination, filters, ContentSort.DEFAULT);
	}

	/**
	 * As above, in the order the entity lists by when the caller names none: the story
	 * entities by number.
	 */
	public ContentSearch toSearch(ContentListRequest request, Pagination pagination, List<ContentFilterValue> filters,
			ContentSort defaultSort) {
		int size = Math.min(Objects.requireNonNullElse(request.size(), pagination.defaultSize()), pagination.maxSize());
		String text = request.q() == null || request.q().isBlank() ? null : request.q().strip();
		return ContentSearch.builder()
			.text(text)
			.filters(filters)
			.sort(Objects.requireNonNullElse(request.sort(), defaultSort))
			.page(Objects.requireNonNullElse(request.page(), 0))
			.size(size)
			.build();
	}

}
