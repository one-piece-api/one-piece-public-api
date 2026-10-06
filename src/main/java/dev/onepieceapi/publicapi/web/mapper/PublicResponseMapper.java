package dev.onepieceapi.publicapi.web.mapper;

import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.Language;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitTypeResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitTypeSummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.LanguageResponse;
import dev.onepieceapi.publicapi.web.dto.response.PageResponse;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

/**
 * From the domain to the bodies of the public contract. The one place that decides what
 * the JSON says, so that a static generator could later produce the same bytes (plan
 * D14).
 */
@UtilityClass
public class PublicResponseMapper {

	public LanguageResponse toResponse(Language language) {
		return new LanguageResponse(language.code(), language.name());
	}

	public DevilFruitTypeResponse toResponse(DevilFruitType devilFruitType) {
		return DevilFruitTypeResponse.builder()
			.id(devilFruitType.id())
			.slug(devilFruitType.slug())
			.romaji(devilFruitType.romaji())
			.language(devilFruitType.language())
			.name(devilFruitType.name())
			.description(devilFruitType.description())
			.advantages(devilFruitType.advantages())
			.disadvantages(devilFruitType.disadvantages())
			.publishedAt(devilFruitType.publishedAt())
			.build();
	}

	public DevilFruitTypeSummaryResponse toResponse(DevilFruitTypeSummary summary) {
		return new DevilFruitTypeSummaryResponse(summary.id(), summary.slug(), summary.romaji(), summary.name());
	}

	public <T, R> PageResponse<R> toResponse(Page<T> page, Function<T, R> itemMapper) {
		return new PageResponse<>(page.content().stream().map(itemMapper).toList(), page.page(), page.size(),
				page.totalElements(), page.totalPages());
	}

}
