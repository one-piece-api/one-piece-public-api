package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A saga online, in one language.
 *
 * @param id the content's identifier
 * @param slug the number, as text
 * @param chapters what its chapters online amount to, through its arcs
 * @param episodes what its episodes online amount to, through its arcs
 * @param arcs its arcs online, by number; empty until added
 * @param publishedAt when the online version was published
 */
@Builder(toBuilder = true)
public record Saga(UUID id, String slug, int number, String language, String title, List<Paragraph> description,
		StorySpan chapters, StorySpan episodes, @With List<ArcSummary> arcs,
		Instant publishedAt) implements PublishedContent, SeenByReader<Saga> {

	public Saga {
		description = description == null ? List.of() : List.copyOf(description);
		arcs = arcs == null ? List.of() : List.copyOf(arcs);
	}

	/** Its description as paragraphs (workflow 3.7.3). */
	public Saga withParagraphs(LongTexts texts) {
		return toBuilder().description(texts.description()).build();
	}

	@Override
	public Saga seenBy(ReaderSight sight) {
		return toBuilder().arcs(sight.shown(this.arcs)).build();
	}

}
