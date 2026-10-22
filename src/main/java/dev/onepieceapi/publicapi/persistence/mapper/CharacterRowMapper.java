package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.AffiliationKind;
import dev.onepieceapi.publicapi.domain.AffiliationReference;
import dev.onepieceapi.publicapi.domain.Character;
import dev.onepieceapi.publicapi.domain.CharacterStatus;
import dev.onepieceapi.publicapi.domain.CharacterSummary;
import dev.onepieceapi.publicapi.domain.DevilFruitReference;
import dev.onepieceapi.publicapi.domain.FirstAppearance;
import dev.onepieceapi.publicapi.domain.FruitUse;
import dev.onepieceapi.publicapi.domain.Gender;
import dev.onepieceapi.publicapi.domain.MarineRank;
import dev.onepieceapi.publicapi.domain.Membership;
import dev.onepieceapi.publicapi.domain.SpeciesReference;
import dev.onepieceapi.publicapi.domain.StoryItemReference;
import dev.onepieceapi.publicapi.persistence.row.CharacterRow;
import dev.onepieceapi.publicapi.persistence.row.CharacterSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.FruitUseRow;
import dev.onepieceapi.publicapi.persistence.row.MembershipRow;
import dev.onepieceapi.publicapi.persistence.row.SpeciesReferenceRow;
import lombok.experimental.UtilityClass;

import java.time.MonthDay;
import java.time.temporal.ChronoUnit;

/** From the rows of {@code published.character} and its child views to the domain. */
@UtilityClass
public class CharacterRowMapper {

	public Character toDomain(CharacterRow row) {
		return Character.builder()
			.id(row.id())
			.slug(row.slug())
			.romaji(row.romaji())
			.japaneseName(row.japaneseName())
			.language(row.language())
			.name(row.name())
			.description(row.description())
			.status(CharacterStatus.valueOf(row.status()))
			.gender(Gender.valueOf(row.gender()))
			.birthday(birthdayOf(row))
			.imageId(row.imageId())
			.publishedAt(row.publishedAt().toInstant().truncatedTo(ChronoUnit.SECONDS))
			.firstAppearance(firstAppearanceOf(row))
			.build();
	}

	public CharacterSummary toDomain(CharacterSummaryRow row) {
		return CharacterSummary.builder()
			.id(row.id())
			.slug(row.slug())
			.romaji(row.romaji())
			.name(row.name())
			.status(CharacterStatus.valueOf(row.status()))
			.imageId(row.imageId())
			.build();
	}

	public SpeciesReference toDomain(SpeciesReferenceRow row) {
		return new SpeciesReference(row.id(), row.slug(), row.name());
	}

	public Membership toDomain(MembershipRow row) {
		AffiliationKind kind = row.kind() == null ? null : AffiliationKind.valueOf(row.kind());
		MarineRank rank = row.marineRank() == null ? null : MarineRank.valueOf(row.marineRank());
		return new Membership(new AffiliationReference(row.id(), row.slug(), row.name(), kind), row.role(), rank,
				row.former());
	}

	/** A fruit of a character: the row names the fruit. */
	public FruitUse toFruitUse(FruitUseRow row) {
		return new FruitUse(new DevilFruitReference(row.id(), row.slug(), row.name()), row.former());
	}

	/** Both or neither, as the version table guarantees. */
	private MonthDay birthdayOf(CharacterRow row) {
		return row.birthdayMonth() == null ? null : MonthDay.of(row.birthdayMonth(), row.birthdayDay());
	}

	private FirstAppearance firstAppearanceOf(CharacterRow row) {
		return FirstAppearance.of(
				StoryItemReference.of(row.firstAppearanceChapterId(), row.firstAppearanceChapterSlug(),
						row.firstAppearanceChapterNumber(), row.firstAppearanceChapterTitle()),
				StoryItemReference.of(row.firstAppearanceEpisodeId(), row.firstAppearanceEpisodeSlug(),
						row.firstAppearanceEpisodeNumber(), row.firstAppearanceEpisodeTitle()));
	}

}
