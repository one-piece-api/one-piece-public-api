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
@Builder
public record BountySummary(UUID id, String slug, String posterName, long amount, BountyCondition condition,
		BountyValidity validity, String imageId, CharacterReference holder) {

}
