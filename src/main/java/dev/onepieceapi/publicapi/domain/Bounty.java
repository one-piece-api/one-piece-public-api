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
 * @param description why it was issued, raised or made inactive, as paragraphs
 * @param imageId the poster's identifier; null when the bounty has none
 * @param publishedAt when the online version was published
 * @param firstAppearance where the reader first meets it
 * @param holder the character the poster is about, as it is online; null when none
 * @param chain the bounties it replaces and that replace it, newest first, itself
 * included; empty until added
 * @param reveals what the reader believes at the first appearance and learns later
 */
@Builder(toBuilder = true)
public record Bounty(UUID id, String slug, String posterName, long amount, BountyCondition condition,
		BountyValidity validity, String language, List<Paragraph> description, String imageId, Instant publishedAt,
		FirstAppearance firstAppearance, CharacterReference holder, @With List<BountySummary> chain,
		FieldReveals<BountyField, BountyValues> reveals) implements PublishedContent, SeenByReader<Bounty> {

	public Bounty {
		description = description == null ? List.of() : List.copyOf(description);
		chain = chain == null ? List.of() : List.copyOf(chain);
		reveals = reveals == null ? FieldReveals.none() : reveals;
	}

	/** Its description as paragraphs (workflow 3.7.3). */
	public Bounty withParagraphs(LongTexts texts) {
		return toBuilder().description(texts.description()).build();
	}

	/**
	 * Its reveals, each reveal of the current value carrying that value (reveals plan R7
	 * S7).
	 */
	public Bounty withReveals(FieldReveals<BountyField, BountyValues> stored) {
		return toBuilder().reveals(stored.filledWith(currentValues())).build();
	}

	/** What the main fields say: whether the poster no longer holds. */
	public BountyValues currentValues() {
		return new BountyValues(this.validity == BountyValidity.INACTIVE);
	}

	@Override
	public Bounty seenBy(ReaderSight sight) {
		return toBuilder().validity(sight.validityAt(this.id, this.validity))
			.holder(CharacterReference.seenBy(this.holder, sight))
			.chain(sight.shown(this.chain))
			.firstAppearance(sight.onAxis(this.firstAppearance))
			.reveals(sight.reached(this.reveals))
			.build();
	}

}
