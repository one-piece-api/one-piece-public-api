package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.bounty}. */
@Builder
public record BountyRow(UUID id, String slug, String posterName, long amount, String condition, String validity,
		String imageId, String language, String description, OffsetDateTime publishedAt, UUID firstAppearanceChapterId,
		String firstAppearanceChapterSlug, Integer firstAppearanceChapterNumber, String firstAppearanceChapterTitle,
		UUID firstAppearanceEpisodeId, String firstAppearanceEpisodeSlug, Integer firstAppearanceEpisodeNumber,
		String firstAppearanceEpisodeTitle) {

}
