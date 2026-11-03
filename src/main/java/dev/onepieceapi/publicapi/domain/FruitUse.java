package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A Devil Fruit of a character online, as of today (Character plan D12).
 *
 * @param former whether the character no longer has its power
 */
public record FruitUse(DevilFruitReference devilFruit, boolean former) {

	/** The fruit eaten. */
	public UUID devilFruitId() {
		return this.devilFruit.id();
	}

}
