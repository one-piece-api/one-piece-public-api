package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.Bounty;
import dev.onepieceapi.publicapi.domain.BountyCondition;
import dev.onepieceapi.publicapi.domain.BountySummary;
import dev.onepieceapi.publicapi.domain.BountyValidity;
import dev.onepieceapi.publicapi.domain.CharacterReference;
import dev.onepieceapi.publicapi.domain.FirstAppearance;
import dev.onepieceapi.publicapi.domain.HeldBounty;
import dev.onepieceapi.publicapi.domain.StoryItemReference;
import dev.onepieceapi.publicapi.persistence.row.BountyRow;
import dev.onepieceapi.publicapi.persistence.row.BountySummaryRow;
import dev.onepieceapi.publicapi.persistence.row.HeldBountyRow;
import lombok.experimental.UtilityClass;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** From the rows of {@code published.bounty} to the domain. */
@UtilityClass
public class BountyRowMapper {

	public Bounty toDomain(BountyRow row) {
		return Bounty.builder()
			.id(row.id())
			.slug(row.slug())
			.posterName(row.posterName())
			.amount(row.amount())
			.condition(BountyCondition.valueOf(row.condition()))
			.validity(BountyValidity.valueOf(row.validity()))
			.language(row.language())
			.description(row.description())
			.imageId(row.imageId())
			.publishedAt(row.publishedAt().toInstant().truncatedTo(ChronoUnit.SECONDS))
			.firstAppearance(firstAppearanceOf(row))
			.holder(holderOf(row.holderId(), row.holderSlug(), row.holderName()))
			.build();
	}

	public BountySummary toDomain(BountySummaryRow row) {
		return BountySummary.builder()
			.id(row.id())
			.slug(row.slug())
			.posterName(row.posterName())
			.amount(row.amount())
			.condition(BountyCondition.valueOf(row.condition()))
			.validity(BountyValidity.valueOf(row.validity()))
			.imageId(row.imageId())
			.holder(holderOf(row.holderId(), row.holderSlug(), row.holderName()))
			.build();
	}

	public HeldBounty toDomain(HeldBountyRow row) {
		return new HeldBounty(row.id(), row.slug(), row.amount(), BountyValidity.valueOf(row.validity()));
	}

	/**
	 * The holder, named in the language when it is written in it; null for a bounty
	 * without one.
	 */
	private CharacterReference holderOf(UUID id, String slug, String name) {
		return id == null ? null : new CharacterReference(id, slug, name);
	}

	private FirstAppearance firstAppearanceOf(BountyRow row) {
		return FirstAppearance.of(
				StoryItemReference.of(row.firstAppearanceChapterId(), row.firstAppearanceChapterSlug(),
						row.firstAppearanceChapterNumber(), row.firstAppearanceChapterTitle()),
				StoryItemReference.of(row.firstAppearanceEpisodeId(), row.firstAppearanceEpisodeSlug(),
						row.firstAppearanceEpisodeNumber(), row.firstAppearanceEpisodeTitle()));
	}

}
