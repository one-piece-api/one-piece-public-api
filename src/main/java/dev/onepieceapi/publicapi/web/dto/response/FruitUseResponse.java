package dev.onepieceapi.publicapi.web.dto.response;

/**
 * A Devil Fruit of a character.
 *
 * @param former whether the character no longer has its power
 */
public record FruitUseResponse(DevilFruitReferenceResponse devilFruit, boolean former) {

}
