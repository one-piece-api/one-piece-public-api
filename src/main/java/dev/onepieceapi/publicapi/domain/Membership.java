package dev.onepieceapi.publicapi.domain;

import lombok.With;

import java.util.List;
import java.util.UUID;

/**
 * A character's place in an affiliation online (Character plan D10): its periods, in
 * order (workflow 3.4.7).
 */
public record Membership(AffiliationReference affiliation, @With List<MembershipPeriod> periods) {

	public Membership {
		periods = List.copyOf(periods);
	}

	/**
	 * The memberships the reader has met with at least one period discovered, each as the
	 * reader sees it.
	 */
	public static List<Membership> metBy(List<Membership> memberships, ReaderSight sight) {
		return sight.seen(memberships, Membership::affiliationId)
			.stream()
			.map((membership) -> membership.seenBy(sight))
			.filter(Membership::known)
			.toList();
	}

	/** The affiliation it is a membership of. */
	public UUID affiliationId() {
		return this.affiliation.id();
	}

	/** The periods the reader has discovered, each as the reader sees it. */
	public Membership seenBy(ReaderSight sight) {
		return withPeriods(sight.discovered(this.periods, MembershipPeriod::withWindow));
	}

	/** Whether the reader knows of at least one of its periods. */
	public boolean known() {
		return !this.periods.isEmpty();
	}

	/** Whether the character no longer belongs to it: none of its periods is current. */
	public boolean former() {
		return this.periods.stream().allMatch(MembershipPeriod::former);
	}

}
