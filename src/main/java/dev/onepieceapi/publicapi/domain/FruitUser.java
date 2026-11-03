package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A character online that has eaten a Devil Fruit, inside the fruit (Character plan D12).
 *
 * @param former whether the character no longer has its power
 */
public record FruitUser(CharacterReference character, boolean former) {

	/** The character who ate it. */
	public UUID characterId() {
		return this.character.id();
	}

}
