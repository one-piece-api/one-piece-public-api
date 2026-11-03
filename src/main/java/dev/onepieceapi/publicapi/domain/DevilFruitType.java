package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A Devil Fruit Type online, in one language.
 *
 * @param id the content's identifier
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param publishedAt when the online version was published
 * @param subcategories its subcategories, in the type's order; empty when it has none
 * @param devilFruits its fruits online, by name (plan D12); empty until added
 * @param firstAppearance where the reader first meets it
 */
@Builder(toBuilder = true)
public record DevilFruitType(UUID id, String slug, String romaji, String language, String name,
		List<Paragraph> description, List<Paragraph> advantages, List<Paragraph> disadvantages, Instant publishedAt,
		@With List<DevilFruitSubcategory> subcategories, @With List<DevilFruitSummary> devilFruits,
		FirstAppearance firstAppearance) implements PublishedContent, SeenByReader<DevilFruitType> {

	public DevilFruitType {
		description = description == null ? List.of() : List.copyOf(description);
		advantages = advantages == null ? List.of() : List.copyOf(advantages);
		disadvantages = disadvantages == null ? List.of() : List.copyOf(disadvantages);
		subcategories = subcategories == null ? List.of() : List.copyOf(subcategories);
		devilFruits = devilFruits == null ? List.of() : List.copyOf(devilFruits);
	}

	/** Its long texts as paragraphs (workflow 3.7.3). */
	public DevilFruitType withParagraphs(LongTexts texts) {
		return toBuilder().description(texts.description())
			.advantages(texts.advantages())
			.disadvantages(texts.disadvantages())
			.build();
	}

	@Override
	public DevilFruitType seenBy(ReaderSight sight) {
		return toBuilder().subcategories(sight.shown(this.subcategories))
			.devilFruits(sight.shown(this.devilFruits))
			.firstAppearance(sight.onAxis(this.firstAppearance))
			.build();
	}

}
