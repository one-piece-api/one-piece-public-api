package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A relative of a character online, as of today (Character plan D13).
 *
 * @param kind what the relative is to the character
 */
public record Relative(RelativeReference character, RelativeKind kind) {

	/**
	 * The same link read from the relative's side: the relative holds this character as
	 * the inverse kind.
	 */
	public Relative inverted() {
		return new Relative(this.character, this.kind.inverse());
	}

	/** The relative. */
	public UUID characterId() {
		return this.character.id();
	}

}
