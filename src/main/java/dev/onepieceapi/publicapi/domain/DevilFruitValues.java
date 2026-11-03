package dev.onepieceapi.publicapi.domain;

import lombok.With;

/**
 * The revealable values of a Devil Fruit (workflow 3.7.1); a field not held is null.
 *
 * @param name in the reader's language
 * @param slug the address derived from the romaji; null when it leaves none
 * @param subcategory null for none, or when the type is not held
 */
public record DevilFruitValues(String name, String romaji, @With String slug, DevilFruitTypeReference type,
		DevilFruitSubcategoryReference subcategory) implements RevealedValues<DevilFruitField, DevilFruitValues> {

	/** Nothing known. */
	public static final DevilFruitValues UNKNOWN = new DevilFruitValues(null, null, null, null, null);

	@Override
	public boolean holds(DevilFruitField field) {
		return switch (field) {
			case NAME -> this.name != null;
			case ROMAJI -> this.romaji != null;
			case TYPE -> this.type != null || this.subcategory != null;
		};
	}

	@Override
	public DevilFruitValues withField(DevilFruitField field, DevilFruitValues other) {
		return switch (field) {
			case NAME -> new DevilFruitValues(other.name, this.romaji, this.slug, this.type, this.subcategory);
			case ROMAJI -> new DevilFruitValues(this.name, other.romaji, other.slug, this.type, this.subcategory);
			case TYPE -> new DevilFruitValues(this.name, this.romaji, this.slug, other.type, other.subcategory);
		};
	}

}
