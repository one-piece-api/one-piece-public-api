package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * A Devil Fruit in one language (Devil Fruit plan D12). Every field is always present,
 * {@code null} when missing; nothing of the editorial workflow shows.
 *
 * @param type its type, in the same language: always present, it is online too
 * @param subcategory the subcategory of its type it names; {@code null} when none
 * @param image the image's path relative to the API root; {@code null} when none
 */
@Builder
public record DevilFruitResponse(UUID id, String slug, String romaji, String language, String name, String description,
		String advantages, String disadvantages, DevilFruitTypeSummaryResponse type,
		DevilFruitSubcategoryResponse subcategory, String image, Instant publishedAt) {

}
