package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * An Affiliation in one language. Every field is always present, {@code null} when
 * missing; nothing of the editorial workflow shows.
 *
 * @param image the emblem's path relative to the API root; {@code null} when none
 * @param firstAppearance where the reader first meets it
 */
@Builder
public record AffiliationResponse(UUID id, String slug, String romaji, String language, String name, String description,
		String image, Instant publishedAt, FirstAppearanceResponse firstAppearance) {

}
