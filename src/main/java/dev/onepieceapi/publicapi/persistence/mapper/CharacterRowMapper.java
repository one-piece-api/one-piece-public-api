package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.AffiliationKind;
import dev.onepieceapi.publicapi.domain.AffiliationReference;
import dev.onepieceapi.publicapi.domain.Character;
import dev.onepieceapi.publicapi.domain.CharacterImage;
import dev.onepieceapi.publicapi.domain.CharacterReference;
import dev.onepieceapi.publicapi.domain.CharacterStatus;
import dev.onepieceapi.publicapi.domain.CharacterSummary;
import dev.onepieceapi.publicapi.domain.DevilFruitReference;
import dev.onepieceapi.publicapi.domain.FirstAppearance;
import dev.onepieceapi.publicapi.domain.FruitUse;
import dev.onepieceapi.publicapi.domain.FruitUser;
import dev.onepieceapi.publicapi.domain.Gender;
import dev.onepieceapi.publicapi.domain.MarineRank;
import dev.onepieceapi.publicapi.domain.Membership;
import dev.onepieceapi.publicapi.domain.MembershipPeriod;
import dev.onepieceapi.publicapi.domain.Relative;
import dev.onepieceapi.publicapi.domain.RelativeKind;
import dev.onepieceapi.publicapi.domain.RelativeReference;
import dev.onepieceapi.publicapi.domain.SpeciesEntry;
import dev.onepieceapi.publicapi.domain.SpeciesReference;
import dev.onepieceapi.publicapi.domain.StoryItemReference;
import dev.onepieceapi.publicapi.domain.TextEntry;
import dev.onepieceapi.publicapi.persistence.row.CharacterRow;
import dev.onepieceapi.publicapi.persistence.row.CharacterSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.FruitUseRow;
import dev.onepieceapi.publicapi.persistence.row.GalleryImageRow;
import dev.onepieceapi.publicapi.persistence.row.MembershipRow;
import dev.onepieceapi.publicapi.persistence.row.RelativeRow;
import dev.onepieceapi.publicapi.persistence.row.SpeciesReferenceRow;
import dev.onepieceapi.publicapi.persistence.row.TextEntryRow;
import dev.onepieceapi.readertime.ImageKind;
import lombok.experimental.UtilityClass;

import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
			.status(CharacterStatus.valueOf(row.status()))
			.gender(Gender.valueOf(row.gender()))
			.birthday(birthdayOf(row))
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
			.build();
	}

	public SpeciesEntry toDomain(SpeciesReferenceRow row) {
		return new SpeciesEntry(new SpeciesReference(row.id(), row.slug(), row.name()),
				ReaderAxisRowMapper.toWindow(row));
	}

	public TextEntry toDomain(TextEntryRow row) {
		return new TextEntry(row.value(), ReaderAxisRowMapper.toWindow(row));
	}

	/**
	 * The memberships of a character from the rows of their periods, ordered by
	 * membership and period: one membership per run of rows of the same membership.
	 */
	public List<Membership> toMemberships(List<MembershipRow> rows) {
		Map<Integer, List<MembershipRow>> byMembership = rows.stream()
			.collect(Collectors.groupingBy(MembershipRow::membershipPosition, LinkedHashMap::new, Collectors.toList()));
		return byMembership.values().stream().map(CharacterRowMapper::toMembership).toList();
	}

	/** A fruit of a character: the row names the fruit. */
	public FruitUse toFruitUse(FruitUseRow row) {
		return new FruitUse(new DevilFruitReference(row.id(), row.slug(), row.name()),
				ReaderAxisRowMapper.toWindow(row));
	}

	/** A user of a fruit: the row names the character. */
	public FruitUser toFruitUser(FruitUseRow row) {
		return new FruitUser(new CharacterReference(row.id(), row.slug(), row.name()),
				ReaderAxisRowMapper.toWindow(row));
	}

	public CharacterImage toDomain(GalleryImageRow row) {
		return new CharacterImage(row.imageId(), ImageKind.valueOf(row.kind()), row.caption(),
				ReaderAxisRowMapper.toWindow(row));
	}

	/**
	 * A relative of a character: the row names the other character and the stored kind.
	 */
	public Relative toRelative(RelativeRow row) {
		return new Relative(new RelativeReference(row.id(), row.slug(), row.name(), Gender.valueOf(row.gender())),
				RelativeKind.valueOf(row.kind()), ReaderAxisRowMapper.toWindow(row));
	}

	/** One membership from the rows of its periods, the first naming the affiliation. */
	private Membership toMembership(List<MembershipRow> periods) {
		MembershipRow first = periods.getFirst();
		AffiliationKind kind = first.kind() == null ? null : AffiliationKind.valueOf(first.kind());
		return new Membership(new AffiliationReference(first.id(), first.slug(), first.name(), kind),
				periods.stream().map(CharacterRowMapper::toPeriod).toList());
	}

	private MembershipPeriod toPeriod(MembershipRow row) {
		MarineRank rank = row.marineRank() == null ? null : MarineRank.valueOf(row.marineRank());
		return new MembershipPeriod(row.role(), rank, ReaderAxisRowMapper.toWindow(row));
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
