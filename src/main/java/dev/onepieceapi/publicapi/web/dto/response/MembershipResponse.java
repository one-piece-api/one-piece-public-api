package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.MarineRank;

/**
 * A membership of a character: its affiliation, and what it is in it.
 *
 * @param role in the language of the path; null when none is written in it
 * @param marineRank null for none; only in a Navy affiliation
 * @param former whether the character no longer belongs to it
 */
public record MembershipResponse(AffiliationReferenceResponse affiliation, String role, MarineRank marineRank,
		boolean former) {

}
