package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * An arc online, in one language.
 *
 * @param id the content's identifier
 * @param slug the number, as text
 * @param saga its saga, in the same language
 * @param chapters what its chapters online amount to
 * @param episodes what its episodes online amount to
 * @param publishedAt when the online version was published
 */
@Builder(toBuilder = true)
public record Arc(UUID id, String slug, int number, String language, String title, String description, SagaSummary saga,
		StorySpan chapters, StorySpan episodes, Instant publishedAt) implements PublishedContent, SeenByReader<Arc> {

	@Override
	public Arc seenBy(ReaderSight sight) {
		return toBuilder().saga(sight.ifSeen(this.saga, SagaSummary::id)).build();
	}

}
