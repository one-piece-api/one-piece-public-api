package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A bounty online, in one language.
 *
 * @param id the content's identifier
 * @param slug the address derived from the poster name and the amount
 * @param amount in Berry
 * @param description why it was issued, raised or made inactive
 * @param imageId the poster's identifier; null when the bounty has none
 * @param publishedAt when the online version was published
 * @param firstAppearance where the reader first meets it
 * @param holder the character the poster is about, as it is online; null when none
 * @param chain the bounties it replaces and that replace it, newest first, itself
 * included; empty until added
 */
@Builder
public record Bounty(UUID id, String slug, String posterName, long amount, BountyCondition condition,
		BountyValidity validity, String language, String description, String imageId, Instant publishedAt,
		FirstAppearance firstAppearance, CharacterReference holder,
		@With List<BountySummary> chain) implements PublishedContent {

	public Bounty {
		chain = chain == null ? List.of() : List.copyOf(chain);
	}

}
