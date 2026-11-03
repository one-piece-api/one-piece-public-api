package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A bounty inside the character that holds it.
 *
 * @param slug the address derived from the poster name and the amount
 * @param amount in Berry
 */
public record HeldBounty(UUID id, String slug, long amount,
		BountyValidity validity) implements SeenByReader<HeldBounty> {

	/** With its validity as known at N (workflow 3.1.8). */
	@Override
	public HeldBounty seenBy(ReaderSight sight) {
		return new HeldBounty(this.id, this.slug, this.amount, sight.validityAt(this.id, this.validity));
	}

}
