package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.chapter}. */
@Builder
public record ChapterRow(UUID id, String slug, int number, String language, String name, String description,
		String japaneseTitle, Integer pageCount, UUID arcId, String arcSlug, int arcNumber, String arcTitle,
		UUID volumeId, String volumeSlug, Integer volumeNumber, String volumeTitle, String volumeCoverImageId,
		OffsetDateTime publishedAt) {

}
