package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.MarineRank;

/**
 * One stay of a character in an affiliation, inside its window (workflow 3.4.7, 3.7.2).
 *
 * @param role in the language of the path; null when none is written in it
 * @param marineRank null for none; only in a Navy affiliation
 * @param former whether the reader knows the stay is over
 * @param discovered where the reader learns it
 * @param ended where the reader learns it is over; {@code null} for none
 */
public record MembershipPeriodResponse(String role, MarineRank marineRank, boolean former,
		StoryPointResponse discovered, StoryPointResponse ended) {

}
