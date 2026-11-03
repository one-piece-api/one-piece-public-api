package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * A Devil Fruit as a row of the list.
 *
 * @param type its type, in the same language
 * @param subcategory the subcategory of its type it names; null when none
 * @param imageId the image's identifier; null when the fruit has none
 * @param firstAppearance where the reader first meets it
 */
@Builder(toBuilder = true)
public record DevilFruitSummary(UUID id, String slug, String romaji, String name, DevilFruitTypeReference type,
		DevilFruitSubcategorySummary subcategory, String imageId,
		FirstAppearance firstAppearance) implements SeenByReader<DevilFruitSummary> {

	@Override
	public DevilFruitSummary seenBy(ReaderSight sight) {
		DevilFruitTypeReference typeSeen = sight.ifSeen(this.type, DevilFruitTypeReference::id);
		return toBuilder().type(typeSeen)
			.subcategory((typeSeen != null) ? sight.ifSeen(this.subcategory, DevilFruitSubcategorySummary::id) : null)
			.firstAppearance(sight.onAxis(this.firstAppearance))
			.build();
	}

}
