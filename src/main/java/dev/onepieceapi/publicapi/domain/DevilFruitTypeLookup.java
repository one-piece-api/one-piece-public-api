package dev.onepieceapi.publicapi.domain;

/**
 * The answer to "which Devil Fruit Type is this address": the type itself, or the current
 * address of the one an old slug used to name (plan D5).
 */
public sealed interface DevilFruitTypeLookup {

	record Found(DevilFruitType devilFruitType) implements DevilFruitTypeLookup {

	}

	record Moved(String currentSlug) implements DevilFruitTypeLookup {

	}

}
