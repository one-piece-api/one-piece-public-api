package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A Devil Fruit Type online, in one language.
 *
 * @param id the content's identifier
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param publishedAt when the online version was published
 * @param devilFruits its fruits online, by name (plan D12); empty until added
 */
@Builder
public record DevilFruitType(UUID id, String slug, String romaji, String language, String name, String description,
		String advantages, String disadvantages, Instant publishedAt,
		@With List<DevilFruitSummary> devilFruits) implements PublishedContent {

	public DevilFruitType {
		devilFruits = devilFruits == null ? List.of() : List.copyOf(devilFruits);
	}

}
