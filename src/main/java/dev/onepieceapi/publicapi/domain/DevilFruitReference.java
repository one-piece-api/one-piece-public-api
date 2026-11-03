package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/** A Devil Fruit inside a character that names it. */
public record DevilFruitReference(UUID id, String slug, String name) {

	/** Named and addressed as the reader knows it at N (reveals plan R7 S10). */
	public DevilFruitReference namedBy(ReaderSight sight) {
		DevilFruitValues known = sight.devilFruitAt(this.id,
				new DevilFruitValues(this.name, null, this.slug, null, null));
		return new DevilFruitReference(this.id, known.slug(), known.name());
	}

}
