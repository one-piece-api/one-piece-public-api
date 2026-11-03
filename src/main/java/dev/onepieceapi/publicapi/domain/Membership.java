package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A character's place in an affiliation online, as of today (Character plan D10).
 *
 * @param role in the reader's language; null when none is written in it
 * @param marineRank null for none
 * @param former whether the character no longer belongs to it
 */
public record Membership(AffiliationReference affiliation, String role, MarineRank marineRank, boolean former) {

	/** The affiliation it is a membership of. */
	public UUID affiliationId() {
		return this.affiliation.id();
	}

}
