package dev.onepieceapi.publicapi.web.mapper;

import dev.onepieceapi.publicapi.domain.DevilFruit;
import dev.onepieceapi.publicapi.domain.DevilFruitSummary;
import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.Language;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitOfTypeResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitSummaryResponse;
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
			.devilFruits(devilFruitType.devilFruits().stream().map(PublicResponseMapper::toTypeFruitResponse).toList())
			.build();
	}

	public DevilFruitTypeSummaryResponse toResponse(DevilFruitTypeSummary summary) {
		return new DevilFruitTypeSummaryResponse(summary.id(), summary.slug(), summary.romaji(), summary.name());
	}

	public DevilFruitResponse toResponse(DevilFruit devilFruit) {
		return DevilFruitResponse.builder()
			.id(devilFruit.id())
			.slug(devilFruit.slug())
			.romaji(devilFruit.romaji())
			.language(devilFruit.language())
			.name(devilFruit.name())
			.description(devilFruit.description())
			.advantages(devilFruit.advantages())
			.disadvantages(devilFruit.disadvantages())
			.type(toResponse(devilFruit.type()))
			.image(imagePath(devilFruit.imageId()))
			.publishedAt(devilFruit.publishedAt())
			.build();
	}

	public DevilFruitSummaryResponse toResponse(DevilFruitSummary summary) {
		return DevilFruitSummaryResponse.builder()
			.id(summary.id())
			.slug(summary.slug())
			.romaji(summary.romaji())
			.name(summary.name())
			.type(toResponse(summary.type()))
			.image(imagePath(summary.imageId()))
			.build();
	}

	/** A fruit inside its type: the type is the enclosing one, so it is left out. */
	public DevilFruitOfTypeResponse toTypeFruitResponse(DevilFruitSummary summary) {
		return new DevilFruitOfTypeResponse(summary.id(), summary.slug(), summary.romaji(), summary.name(),
				imagePath(summary.imageId()));
	}

	public <T, R> PageResponse<R> toResponse(Page<T> page, Function<T, R> itemMapper) {
		return new PageResponse<>(page.content().stream().map(itemMapper).toList(), page.page(), page.size(),
				page.totalElements(), page.totalPages());
	}

	/**
	 * The address of an image relative to the API root, as every path the API gives (plan
	 * D7); {@code null} when there is none.
	 */
	private String imagePath(String imageId) {
		return imageId == null ? null : ApiPaths.IMAGE.substring(1).replace("{id}", imageId);
	}

}
