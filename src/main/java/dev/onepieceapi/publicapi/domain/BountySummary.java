package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * A bounty as a row of the list, or as a link of a chain.
 *
 * @param slug the address derived from the poster name and the amount
 * @param amount in Berry
 * @param imageId the poster's identifier; null when the bounty has none
 * @param holder the character the poster is about, as it is online; null when none
 */
@Builder(toBuilder = true)
public record BountySummary(UUID id, String slug, String posterName, long amount, BountyCondition condition,
		BountyValidity validity, String imageId, CharacterReference holder) implements SeenByReader<BountySummary> {

	/** With its validity as known at N (workflow 3.1.8) and its holder as met by then. */
	@Override
	public BountySummary seenBy(ReaderSight sight) {
		return toBuilder().validity(sight.validityAt(this.id, this.validity))
			.holder(CharacterReference.seenBy(this.holder, sight))
			.build();
	}

}
