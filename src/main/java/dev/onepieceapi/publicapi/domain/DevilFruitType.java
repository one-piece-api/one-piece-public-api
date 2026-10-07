package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * A Devil Fruit Type online, in one language.
 *
 * @param id the content's identifier
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param publishedAt when the online version was published
 */
@Builder
public record DevilFruitType(UUID id, String slug, String romaji, String language, String name, String description,
		String advantages, String disadvantages, Instant publishedAt) implements PublishedContent {

}
