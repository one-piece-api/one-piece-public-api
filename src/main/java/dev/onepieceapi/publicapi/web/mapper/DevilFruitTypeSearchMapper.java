package dev.onepieceapi.publicapi.web.mapper;

import dev.onepieceapi.publicapi.config.PublicApiProperties.Pagination;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSearch;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSort;
import dev.onepieceapi.publicapi.web.dto.request.DevilFruitTypeListRequest;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/** From the query parameters of the list to what the service searches for. */
@UtilityClass
public class DevilFruitTypeSearchMapper {

	/** Applies the defaults and the page size cap (plan D8). */
	public DevilFruitTypeSearch toSearch(DevilFruitTypeListRequest request, Pagination pagination) {
		int size = Math.min(Objects.requireNonNullElse(request.size(), pagination.defaultSize()), pagination.maxSize());
		String text = request.q() == null || request.q().isBlank() ? null : request.q().strip();
		return new DevilFruitTypeSearch(text, Objects.requireNonNullElse(request.sort(), DevilFruitTypeSort.DEFAULT),
				Objects.requireNonNullElse(request.page(), 0), size);
	}

}
