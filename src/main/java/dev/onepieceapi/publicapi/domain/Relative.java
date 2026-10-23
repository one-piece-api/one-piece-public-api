package dev.onepieceapi.publicapi.domain;

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

}
