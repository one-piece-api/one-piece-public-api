package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A saga in one language. Every field is always present, {@code null} when missing;
 * nothing of the editorial workflow shows.
 *
 * @param chapters what its chapters online amount to, through its arcs
 * @param episodes what its episodes online amount to, through its arcs
 * @param arcs its arcs online, by number
 */
@Builder
public record SagaResponse(UUID id, String slug, int number, String language, String title, String description,
		StorySpanResponse chapters, StorySpanResponse episodes, List<ArcReferenceResponse> arcs, Instant publishedAt) {

}
