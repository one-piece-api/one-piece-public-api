package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * An Affiliation online, in one language (Affiliation plan D1-D5).
 *
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param kind null for a version written before kinds existed
 * @param imageId the emblem's identifier; null when it has none
 * @param publishedAt when the online version was published
 * @param firstAppearance where the reader first meets it
 * @param memberCount how many characters online belong to it now, those who left it not
 * counted
 */
@Builder(toBuilder = true)
public record Affiliation(UUID id, String slug, String romaji, String language, String name,
		List<Paragraph> description, AffiliationKind kind, String imageId, Instant publishedAt,
		FirstAppearance firstAppearance,
		@With long memberCount) implements PublishedContent, SeenByReader<Affiliation> {

	public Affiliation {
		description = description == null ? List.of() : List.copyOf(description);
	}

	/** Its description as paragraphs (workflow 3.7.3). */
	public Affiliation withParagraphs(LongTexts texts) {
		return toBuilder().description(texts.description()).build();
	}

	@Override
	public Affiliation seenBy(ReaderSight sight) {
		return toBuilder().firstAppearance(sight.onAxis(this.firstAppearance)).build();
	}

}
