package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.AffiliationKind;
import dev.onepieceapi.publicapi.domain.AffiliationReference;
import dev.onepieceapi.publicapi.domain.DevilFruitReference;
import dev.onepieceapi.publicapi.domain.FruitUse;
import dev.onepieceapi.publicapi.domain.Membership;
import dev.onepieceapi.publicapi.domain.MembershipPeriod;
import dev.onepieceapi.publicapi.domain.SpeciesEntry;
import dev.onepieceapi.publicapi.domain.SpeciesReference;
import dev.onepieceapi.publicapi.persistence.row.EntryRow;
import dev.onepieceapi.publicapi.persistence.row.MembershipEntryRow;
import lombok.experimental.UtilityClass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * From the lean rows of the character lists that filters test to the domain: the
 * references carry the identifier only, as a filter needs nothing else.
 */
@UtilityClass
public class CharacterEntryRowMapper {

	public Map<UUID, List<SpeciesEntry>> toSpecies(List<EntryRow> rows) {
		return rows.stream()
			.collect(Collectors.groupingBy(EntryRow::characterId, LinkedHashMap::new,
					Collectors.mapping(CharacterEntryRowMapper::toSpeciesEntry, Collectors.toList())));
	}

	public Map<UUID, List<FruitUse>> toDevilFruits(List<EntryRow> rows) {
		return rows.stream()
			.collect(Collectors.groupingBy(EntryRow::characterId, LinkedHashMap::new,
					Collectors.mapping(CharacterEntryRowMapper::toFruitUse, Collectors.toList())));
	}

	/** One membership per run of rows of the same character and membership. */
	public Map<UUID, List<Membership>> toMemberships(List<MembershipEntryRow> rows) {
		Map<UUID, Map<Integer, List<MembershipEntryRow>>> grouped = rows.stream()
			.collect(Collectors.groupingBy(MembershipEntryRow::characterId, LinkedHashMap::new, Collectors
				.groupingBy(MembershipEntryRow::membershipPosition, LinkedHashMap::new, Collectors.toList())));
		Map<UUID, List<Membership>> memberships = new LinkedHashMap<>();
		grouped.forEach((characterId, byPosition) -> memberships.put(characterId,
				byPosition.values().stream().map(CharacterEntryRowMapper::toMembership).toList()));
		return memberships;
	}

	private SpeciesEntry toSpeciesEntry(EntryRow row) {
		return new SpeciesEntry(new SpeciesReference(row.id(), null, null), ReaderAxisRowMapper.toWindow(row));
	}

	private FruitUse toFruitUse(EntryRow row) {
		return new FruitUse(new DevilFruitReference(row.id(), null, null), ReaderAxisRowMapper.toWindow(row));
	}

	private Membership toMembership(List<MembershipEntryRow> periods) {
		MembershipEntryRow first = periods.getFirst();
		AffiliationKind kind = (first.kind() == null) ? null : AffiliationKind.valueOf(first.kind());
		return new Membership(new AffiliationReference(first.id(), null, null, kind),
				periods.stream()
					.map((row) -> new MembershipPeriod(null, null, ReaderAxisRowMapper.toWindow(row)))
					.toList());
	}

}
