package dev.onepieceapi.publicapi.web.dto.response;

/**
 * A species of a character, inside its window (workflow 3.7.2).
 *
 * @param former whether the reader knows it no longer holds
 * @param discovered where the reader learns it
 * @param ended where the reader learns it is over; {@code null} for none
 */
public record SpeciesEntryResponse(SpeciesReferenceResponse species, boolean former, StoryPointResponse discovered,
		StoryPointResponse ended) {

}
