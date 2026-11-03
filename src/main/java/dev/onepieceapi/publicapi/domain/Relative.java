package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.RevealWindow;
import lombok.With;

import java.util.UUID;

/**
 * A relative of a character online (Character plan D13), inside the window of the
 * character that stores the link (workflow 3.7.2).
 *
 * @param kind what the relative is to the character
 */
public record Relative(RelativeReference character, RelativeKind kind,
		@With RevealWindow window) implements WindowedEntry {

	/**
	 * The same link read from the relative's side: the relative holds this character as
	 * the inverse kind, in the same window.
	 */
	public Relative inverted() {
		return new Relative(this.character, this.kind.inverse(), this.window);
	}

	/** The relative. */
	public UUID characterId() {
		return this.character.id();
	}

}
