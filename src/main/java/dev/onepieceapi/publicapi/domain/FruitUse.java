package dev.onepieceapi.publicapi.domain;

/**
 * A Devil Fruit of a character online, as of today (Character plan D12).
 *
 * @param former whether the character no longer has its power
 */
public record FruitUse(DevilFruitReference devilFruit, boolean former) {

}
