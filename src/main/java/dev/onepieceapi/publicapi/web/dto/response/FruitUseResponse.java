package dev.onepieceapi.publicapi.web.dto.response;

/**
 * A Devil Fruit of a character, inside its window (workflow 3.7.2).
 *
 * @param former whether the character no longer has its power
 * @param discovered where the reader learns it
 * @param ended where the reader learns it is over; {@code null} for none
 */
public record FruitUseResponse(DevilFruitReferenceResponse devilFruit, boolean former, StoryPointResponse discovered,
		StoryPointResponse ended) {

}
