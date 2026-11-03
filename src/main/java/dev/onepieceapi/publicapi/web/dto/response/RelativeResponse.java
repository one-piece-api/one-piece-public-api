package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.RelativeKind;

/**
 * A relative of a character, inside the window of the character that stores the link
 * (workflow 3.7.2).
 *
 * @param kind what the relative is to the character
 * @param former whether the reader knows the link no longer holds
 * @param discovered where the reader learns it
 * @param ended where the reader learns it is over; {@code null} for none
 */
public record RelativeResponse(RelativeReferenceResponse character, RelativeKind kind, boolean former,
		StoryPointResponse discovered, StoryPointResponse ended) {

}
