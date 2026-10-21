package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.Affiliation;
import dev.onepieceapi.publicapi.domain.AffiliationKind;
import dev.onepieceapi.publicapi.domain.AffiliationSummary;
import dev.onepieceapi.publicapi.domain.FirstAppearance;
import dev.onepieceapi.publicapi.domain.StoryItemReference;
import dev.onepieceapi.publicapi.persistence.row.AffiliationRow;
import dev.onepieceapi.publicapi.persistence.row.AffiliationSummaryRow;
import lombok.experimental.UtilityClass;

import java.time.temporal.ChronoUnit;

/** From the rows of {@code published.affiliation} to the domain. */
@UtilityClass
public class AffiliationRowMapper {

	public Affiliation toDomain(AffiliationRow row) {
		return Affiliation.builder()
			.id(row.id())
			.slug(row.slug())
			.romaji(row.romaji())
			.language(row.language())
			.name(row.name())
			.description(row.description())
			.kind(kindOf(row.kind()))
			.imageId(row.imageId())
			.publishedAt(row.publishedAt().toInstant().truncatedTo(ChronoUnit.SECONDS))
			.firstAppearance(firstAppearanceOf(row))
			.build();
	}

	public AffiliationSummary toDomain(AffiliationSummaryRow row) {
		return AffiliationSummary.builder()
			.id(row.id())
			.slug(row.slug())
			.romaji(row.romaji())
			.name(row.name())
			.kind(kindOf(row.kind()))
			.imageId(row.imageId())
			.build();
	}

	/** Null for a version written before kinds existed. */
	private AffiliationKind kindOf(String kind) {
		return kind == null ? null : AffiliationKind.valueOf(kind);
	}

	private FirstAppearance firstAppearanceOf(AffiliationRow row) {
		return FirstAppearance.of(
				StoryItemReference.of(row.firstAppearanceChapterId(), row.firstAppearanceChapterSlug(),
						row.firstAppearanceChapterNumber(), row.firstAppearanceChapterTitle()),
				StoryItemReference.of(row.firstAppearanceEpisodeId(), row.firstAppearanceEpisodeSlug(),
						row.firstAppearanceEpisodeNumber(), row.firstAppearanceEpisodeTitle()));
	}

}
