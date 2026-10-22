package dev.onepieceapi.publicapi.domain;

/**
 * A character online that has eaten a Devil Fruit, inside the fruit (Character plan D12).
 *
 * @param former whether the character no longer has its power
 */
public record FruitUser(CharacterReference character, boolean former) {

}
