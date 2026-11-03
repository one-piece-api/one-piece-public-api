package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.RevealWindow;
import lombok.With;

import java.util.UUID;

/**
 * A relative of a character online (Character plan D13), inside the window of the
 * character that stores the link (workflow 3.7.2).
 *
 * @param kind what the relative is to the character
 * @param inverse whether the link is stored by the relative, read here through the
 * inverse kind
 */
public record Relative(@With RelativeReference character, RelativeKind kind, @With RevealWindow window,
		boolean inverse) implements WindowedEntry {

	/** A link the character stores. */
	public Relative(RelativeReference character, RelativeKind kind, RevealWindow window) {
		this(character, kind, window, false);
	}

	/**
	 * The same link read from the relative's side: the relative holds this character as
	 * the inverse kind, in the same window.
	 */
	public Relative inverted() {
		return new Relative(this.character, this.kind.inverse(), this.window, true);
	}

	/** The relative. */
	public UUID characterId() {
		return this.character.id();
	}

	/** The relative's name; null when not written in the language. */
	public String name() {
		return this.character.name();
	}

	/** The relative named as the reader knows it at N (reveals plan R7 S10). */
	public Relative namedBy(ReaderSight sight) {
		return withCharacter(this.character.namedBy(sight));
	}

}
