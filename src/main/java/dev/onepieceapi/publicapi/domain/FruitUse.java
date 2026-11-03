package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.RevealWindow;
import lombok.With;

import java.util.UUID;

/**
 * A Devil Fruit of a character online, inside its window (Character plan D12; workflow
 * 3.7.2): former once the reader knows the character no longer has its power.
 */
public record FruitUse(@With DevilFruitReference devilFruit, @With RevealWindow window) implements WindowedEntry {

	/** The fruit eaten. */
	public UUID devilFruitId() {
		return this.devilFruit.id();
	}

	/** The fruit named as the reader knows it at N (reveals plan R7 S10). */
	public FruitUse namedBy(ReaderSight sight) {
		return withDevilFruit(this.devilFruit.namedBy(sight));
	}

}
