package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A Species online, in one language (Species plan D1-D5).
 *
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param imageId the picture's identifier; null when it has none
 * @param publishedAt when the online version was published
 * @param firstAppearance where the reader first meets it
 */
@Builder(toBuilder = true)
public record Species(UUID id, String slug, String romaji, String language, String name, List<Paragraph> description,
		String imageId, Instant publishedAt,
		FirstAppearance firstAppearance) implements PublishedContent, SeenByReader<Species> {

	public Species {
		description = description == null ? List.of() : List.copyOf(description);
	}

	/** Its description as paragraphs (workflow 3.7.3). */
	public Species withParagraphs(LongTexts texts) {
		return toBuilder().description(texts.description()).build();
	}

	@Override
	public Species seenBy(ReaderSight sight) {
		return toBuilder().firstAppearance(sight.onAxis(this.firstAppearance)).build();
	}

}
