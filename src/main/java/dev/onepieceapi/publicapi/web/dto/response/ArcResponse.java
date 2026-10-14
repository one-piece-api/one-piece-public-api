package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * An arc in one language.
 *
 * @param saga its saga, in the same language: always present, it is online too
 * @param chapters what its chapters online amount to
 * @param episodes what its episodes online amount to
 */
@Builder
public record ArcResponse(UUID id, String slug, int number, String language, String title, String description,
		SagaSummaryResponse saga, StorySpanResponse chapters, StorySpanResponse episodes, Instant publishedAt) {

}
