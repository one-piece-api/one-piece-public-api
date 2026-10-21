package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.character}. */
@Builder
public record CharacterRow(UUID id, String slug, String romaji, String japaneseName, String language, String name,
		String description, String status, String gender, Integer birthdayMonth, Integer birthdayDay, String imageId,
		OffsetDateTime publishedAt, UUID firstAppearanceChapterId, String firstAppearanceChapterSlug,
		Integer firstAppearanceChapterNumber, String firstAppearanceChapterTitle, UUID firstAppearanceEpisodeId,
		String firstAppearanceEpisodeSlug, Integer firstAppearanceEpisodeNumber, String firstAppearanceEpisodeTitle) {

}
