package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.saga}. */
@Builder
public record SagaRow(UUID id, String slug, int number, String language, String name, String description,
		OffsetDateTime publishedAt, int chapterCount, Integer firstChapterNumber, Integer lastChapterNumber,
		LocalDate firstChapterDate, LocalDate lastChapterDate, int episodeCount, Integer firstEpisodeNumber,
		Integer lastEpisodeNumber, LocalDate firstEpisodeDate, LocalDate lastEpisodeDate) {

}
