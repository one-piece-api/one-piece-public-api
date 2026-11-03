package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.RevealWindow;
import lombok.With;

import java.util.UUID;

/**
 * A character online that has eaten a Devil Fruit, inside the fruit (Character plan D12),
 * with the window the character stores for it (workflow 3.7.2).
 */
public record FruitUser(CharacterReference character, @With RevealWindow window) implements WindowedEntry {

	/** The character who ate it. */
	public UUID characterId() {
		return this.character.id();
	}

	/** The name of the character who ate it; null when not written in the language. */
	public String name() {
		return this.character.name();
	}

}
