package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.arc}. */
@Builder
public record ArcRow(UUID id, String slug, int number, String language, String name, String description, UUID sagaId,
		String sagaSlug, int sagaNumber, String sagaTitle, OffsetDateTime publishedAt, int chapterCount,
		Integer firstChapterNumber, Integer lastChapterNumber, LocalDate firstChapterDate, LocalDate lastChapterDate,
		int episodeCount, Integer firstEpisodeNumber, Integer lastEpisodeNumber, LocalDate firstEpisodeDate,
		LocalDate lastEpisodeDate) {

}
