package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.BountyCondition;
import dev.onepieceapi.publicapi.domain.BountyValidity;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A bounty in one language (Bounty plan D5). Every field is always present, {@code null}
 * when missing; nothing of the editorial workflow shows.
 *
 * @param slug the address derived from the poster name and the amount
 * @param posterName the name printed on the poster
 * @param amount in Berry
 * @param validity derived from the bounties online
 * @param description why it was issued, raised or made inactive
 * @param image the poster's path relative to the API root; {@code null} when none
 * @param firstAppearance where the reader first meets it
 * @param holder the character the poster is about; {@code null} when none
 * @param chain the bounties it replaces and that replace it, newest first, itself
 * included
 */
@Builder
public record BountyResponse(UUID id, String slug, String posterName, long amount, BountyCondition condition,
		BountyValidity validity, String language, String description, String image, Instant publishedAt,
		FirstAppearanceResponse firstAppearance, CharacterReferenceResponse holder, List<BountySummaryResponse> chain) {

}
