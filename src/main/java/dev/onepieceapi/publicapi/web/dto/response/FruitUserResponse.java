package dev.onepieceapi.publicapi.web.dto.response;

/**
 * A character that has eaten a Devil Fruit.
 *
 * @param former whether the character no longer has its power
 */
public record FruitUserResponse(CharacterReferenceResponse character, boolean former) {

}
