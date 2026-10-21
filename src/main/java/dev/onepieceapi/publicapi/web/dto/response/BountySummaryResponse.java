package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.BountyCondition;
import dev.onepieceapi.publicapi.domain.BountyValidity;
import lombok.Builder;

import java.util.UUID;

/**
 * A row of the list of bounties, or a link of a chain.
 *
 * @param slug the address derived from the poster name and the amount
 * @param posterName the name printed on the poster
 * @param amount in Berry
 * @param validity derived from the bounties online
 * @param image the poster's path relative to the API root; {@code null} when none
 * @param holder the character the poster is about; {@code null} when none
 */
@Builder
public record BountySummaryResponse(UUID id, String slug, String posterName, long amount, BountyCondition condition,
		BountyValidity validity, String image, CharacterReferenceResponse holder) {

}
