package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.species}. */
@Builder
public record SpeciesRow(UUID id, String slug, String romaji, String language, String name, String description,
		String imageId, OffsetDateTime publishedAt, UUID firstAppearanceChapterId, String firstAppearanceChapterSlug,
		Integer firstAppearanceChapterNumber, String firstAppearanceChapterTitle, UUID firstAppearanceEpisodeId,
		String firstAppearanceEpisodeSlug, Integer firstAppearanceEpisodeNumber, String firstAppearanceEpisodeTitle) {

}
