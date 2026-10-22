package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

/**
 * An Affiliation online, in one language (Affiliation plan D1-D5).
 *
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param kind null for a version written before kinds existed
 * @param imageId the emblem's identifier; null when it has none
 * @param publishedAt when the online version was published
 * @param firstAppearance where the reader first meets it
 * @param memberCount how many characters online belong to it now, those who left it not
 * counted
 */
@Builder
public record Affiliation(UUID id, String slug, String romaji, String language, String name, String description,
		AffiliationKind kind, String imageId, Instant publishedAt, FirstAppearance firstAppearance,
		@With long memberCount) implements PublishedContent {

}
