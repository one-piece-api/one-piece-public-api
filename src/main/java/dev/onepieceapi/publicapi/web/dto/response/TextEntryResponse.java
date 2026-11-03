package dev.onepieceapi.publicapi.web.dto.response;

/**
 * An epithet or an alias, inside its window (workflow 3.7.2).
 *
 * @param value an epithet in the language of the path, an alias the same in every
 * language
 * @param former whether the reader knows it no longer holds
 * @param discovered where the reader learns it
 * @param ended where the reader learns it is over; {@code null} for none
 */
public record TextEntryResponse(String value, boolean former, StoryPointResponse discovered, StoryPointResponse ended) {

}
