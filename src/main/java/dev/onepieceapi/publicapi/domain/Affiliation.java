package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * An Affiliation online, in one language (Affiliation plan D1-D5).
 *
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param imageId the emblem's identifier; null when it has none
 * @param publishedAt when the online version was published
 * @param firstAppearance where the reader first meets it
 */
@Builder
public record Affiliation(UUID id, String slug, String romaji, String language, String name, String description,
		String imageId, Instant publishedAt, FirstAppearance firstAppearance) implements PublishedContent {

}
