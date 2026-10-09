package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * A Devil Fruit online, in one language.
 *
 * @param id the content's identifier
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param type its type, in the same language
 * @param imageId the image's identifier; null when the fruit has none
 * @param publishedAt when the online version was published
 */
@Builder
public record DevilFruit(UUID id, String slug, String romaji, String language, String name, String description,
		String advantages, String disadvantages, DevilFruitTypeSummary type, String imageId,
		Instant publishedAt) implements PublishedContent {

}
