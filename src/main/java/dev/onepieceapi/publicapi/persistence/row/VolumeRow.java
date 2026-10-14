package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.volume}. */
@Builder
public record VolumeRow(UUID id, String slug, int number, String language, String name, String japaneseTitle,
		String coverImageId, OffsetDateTime publishedAt, int chapterCount, Integer firstChapterNumber,
		Integer lastChapterNumber, LocalDate firstChapterDate, LocalDate lastChapterDate) {

}
