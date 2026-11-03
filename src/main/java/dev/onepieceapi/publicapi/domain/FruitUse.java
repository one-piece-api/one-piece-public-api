package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.RevealWindow;

import java.util.UUID;

/**
 * A Devil Fruit of a character online, inside its window (Character plan D12; workflow
 * 3.7.2): former once the reader knows the character no longer has its power.
 */
public record FruitUse(DevilFruitReference devilFruit, RevealWindow window) implements WindowedEntry {

	/** The fruit eaten. */
	public UUID devilFruitId() {
		return this.devilFruit.id();
	}

}
