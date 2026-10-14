package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * An episode in one language.
 *
 * @param japaneseTitle the title as published in Japan, one for every language;
 * {@code null} when unknown
 * @param arc its arc; {@code null} for an episode outside any arc (a filler)
 * @param broadcasts its broadcast day in every country it has one, by day
 */
@Builder
public record EpisodeResponse(UUID id, String slug, int number, String language, String title, String description,
		String japaneseTitle, ArcReferenceResponse arc, List<ReleaseResponse> broadcasts, Instant publishedAt) {

}
