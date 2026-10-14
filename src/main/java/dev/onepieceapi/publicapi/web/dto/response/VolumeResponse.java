package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A volume in one language.
 *
 * @param japaneseTitle the title as published in Japan, one for every language;
 * {@code null} when unknown
 * @param cover the cover image path relative to the API root; {@code null} when none
 * @param chapters what its chapters online amount to
 * @param arcs the arcs of those chapters, by number
 * @param releases its release day in every country it has one, by day
 */
@Builder
public record VolumeResponse(UUID id, String slug, int number, String language, String title, String japaneseTitle,
		String cover, StorySpanResponse chapters, List<ArcReferenceResponse> arcs, List<ReleaseResponse> releases,
		Instant publishedAt) {

}
