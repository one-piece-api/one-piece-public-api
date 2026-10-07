package dev.onepieceapi.publicapi.web.dto.request;

import dev.onepieceapi.publicapi.domain.ContentSort;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * The query parameters of the list (plan D8). All optional; a parameter nobody declared
 * is ignored.
 *
 * @param page 0-based, 0 when missing
 * @param size the default size when missing, capped to the configured maximum when larger
 * @param sort {@code field} or {@code field,direction}
 * @param q what the name or the romaji contains
 */
public record ContentListRequest(@Parameter(description = "0-based page, 0 when missing") @Min(0) Integer page,
		@Parameter(description = "Page size: 20 when missing, at most 100") @Min(1) Integer size,
		@Parameter(hidden = true) ContentSort sort,
		@Parameter(
				description = "Text the name or the romaji contains, case-insensitive; 100 characters at most") @Size(
						max = ContentListRequest.MAX_QUERY_LENGTH) String q) {

	/**
	 * Longest {@code q} served: a longer one costs the database more than it is worth.
	 */
	public static final int MAX_QUERY_LENGTH = 100;

}
