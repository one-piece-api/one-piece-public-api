package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A chapter in one language.
 *
 * @param japaneseTitle the title as published in Japan, one for every language;
 * {@code null} when unknown
 * @param pageCount {@code null} when unknown
 * @param arc its arc, in the same language: always present, it is online too
 * @param volume its volume; {@code null} when it is not collected in one yet
 * @param releases its release day in every country it has one, by day
 */
@Builder
public record ChapterResponse(UUID id, String slug, int number, String language, String title, String description,
		String japaneseTitle, Integer pageCount, ArcReferenceResponse arc, VolumeSummaryResponse volume,
		List<ReleaseResponse> releases, Instant publishedAt) {

}
