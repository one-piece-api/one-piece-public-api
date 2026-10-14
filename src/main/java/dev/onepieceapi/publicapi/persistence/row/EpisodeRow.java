package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.episode}. */
@Builder
public record EpisodeRow(UUID id, String slug, int number, String language, String name, String description,
		String japaneseTitle, UUID arcId, String arcSlug, Integer arcNumber, String arcTitle,
		OffsetDateTime publishedAt) {

}
