package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.affiliation}. */
@Builder
public record AffiliationRow(UUID id, String slug, String romaji, String language, String name, String description,
		String kind, String imageId, OffsetDateTime publishedAt, UUID firstAppearanceChapterId,
		String firstAppearanceChapterSlug, Integer firstAppearanceChapterNumber, String firstAppearanceChapterTitle,
		UUID firstAppearanceEpisodeId, String firstAppearanceEpisodeSlug, Integer firstAppearanceEpisodeNumber,
		String firstAppearanceEpisodeTitle) {

}
