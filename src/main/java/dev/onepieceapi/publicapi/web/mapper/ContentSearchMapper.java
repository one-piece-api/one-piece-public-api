package dev.onepieceapi.publicapi.web.mapper;

import dev.onepieceapi.publicapi.config.PublicApiProperties.Pagination;
import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.web.dto.request.ContentListRequest;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;

/** From the query parameters of the list to what the service searches for. */
@UtilityClass
public class ContentSearchMapper {

	/** Applies the defaults and the page size cap (plan D8). */
	public ContentSearch toSearch(ContentListRequest request, Pagination pagination) {
		return toSearch(request, pagination, List.of());
	}

	/** As above, narrowed by the filters of the entity its controller read (plan D14). */
	public ContentSearch toSearch(ContentListRequest request, Pagination pagination, List<ContentFilterValue> filters) {
		int size = Math.min(Objects.requireNonNullElse(request.size(), pagination.defaultSize()), pagination.maxSize());
		String text = request.q() == null || request.q().isBlank() ? null : request.q().strip();
		return ContentSearch.builder()
			.text(text)
			.filters(filters)
			.sort(Objects.requireNonNullElse(request.sort(), ContentSort.DEFAULT))
			.page(Objects.requireNonNullElse(request.page(), 0))
			.size(size)
			.build();
	}

}
