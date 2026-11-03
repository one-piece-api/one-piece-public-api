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

	/**
	 * Named, addressed and typed as known at N (workflow 3.7.1); a type not met yet left
	 * out with its subcategory.
	 */
	@Override
	public DevilFruitSummary seenBy(ReaderSight sight) {
		DevilFruitValues known = sight.devilFruitAt(this.id, currentValues());
		DevilFruitTypeReference typeSeen = sight.ifSeen(known.type(), DevilFruitTypeReference::id);
		DevilFruitSubcategoryReference subcategorySeen = (typeSeen != null)
				? sight.ifSeen(known.subcategory(), DevilFruitSubcategoryReference::id) : null;
		return toBuilder().name(known.name())
			.romaji(known.romaji())
			.slug(known.slug())
			.type(typeSeen)
			.subcategory((subcategorySeen != null) ? subcategorySeen.summary() : null)
			.firstAppearance(sight.onAxis(this.firstAppearance))
			.build();
	}

	/** What the row says: the current value of every revealable field. */
	private DevilFruitValues currentValues() {
		DevilFruitSubcategoryReference current = (this.subcategory != null)
				? new DevilFruitSubcategoryReference(this.subcategory.id(), this.subcategory.name(), null) : null;
		return new DevilFruitValues(this.name, this.romaji, this.slug, this.type, current);
	}

}
