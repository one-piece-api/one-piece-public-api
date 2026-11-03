package dev.onepieceapi.publicapi.web.dto.response;

import java.util.List;

/**
 * A membership of a character: its affiliation and its periods (workflow 3.4.7).
 *
 * @param former whether the character no longer belongs to it: none of its periods is
 * current
 * @param periods its stays, in order
 */
public record MembershipResponse(AffiliationReferenceResponse affiliation, boolean former,
		List<MembershipPeriodResponse> periods) {

}
