package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A Devil Fruit Type in one language. Every field is always present, {@code null} when
 * missing (plan D7); nothing of the editorial workflow shows.
 *
 * @param subcategories its subcategories, in order; empty when it has none
 * @param devilFruits all its fruits online, by name (Devil Fruit plan D12)
 */
@Builder
public record DevilFruitTypeResponse(UUID id, String slug, String romaji, String language, String name,
		String description, String advantages, String disadvantages, Instant publishedAt,
		List<DevilFruitSubcategoryResponse> subcategories, List<DevilFruitOfTypeResponse> devilFruits) {

}
