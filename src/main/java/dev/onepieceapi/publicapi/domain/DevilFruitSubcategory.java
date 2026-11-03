package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A subcategory of a Devil Fruit Type online, in one language (subcategories plan S9).
 *
 * @param firstAppearance where the reader first meets it; null when not given
 */
public record DevilFruitSubcategory(UUID id, String name, String description,
		FirstAppearance firstAppearance) implements SeenByReader<DevilFruitSubcategory> {

	@Override
	public DevilFruitSubcategory seenBy(ReaderSight sight) {
		return new DevilFruitSubcategory(this.id, this.name, this.description, sight.onAxis(this.firstAppearance));
	}

}
