package dev.onepieceapi.publicapi.domain;

import java.util.List;
import java.util.UUID;

/**
 * A character's place in an affiliation online (Character plan D10): its periods, in
 * order (workflow 3.4.7).
 */
public record Membership(AffiliationReference affiliation, List<MembershipPeriod> periods) {

	public Membership {
		periods = List.copyOf(periods);
	}

	/** The affiliation it is a membership of. */
	public UUID affiliationId() {
		return this.affiliation.id();
	}

	/** Whether the character no longer belongs to it: none of its periods is current. */
	public boolean former() {
		return this.periods.stream().allMatch(MembershipPeriod::former);
	}

}
