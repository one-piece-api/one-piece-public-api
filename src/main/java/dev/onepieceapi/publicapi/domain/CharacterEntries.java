package dev.onepieceapi.publicapi.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The lists of the characters online that a list filter tests (species, memberships,
 * Devil Fruits), read once per filtered request. Each test sees them as the reader at N
 * does, with the same rules as the detail (workflow 3.7.2, 3.8), so a filter and the
 * detail of a character never disagree.
 */
public record CharacterEntries(Map<UUID, List<SpeciesEntry>> species, Map<UUID, List<Membership>> memberships,
		Map<UUID, List<FruitUse>> devilFruits) {

	/** No lists: for the entities whose filters do not test any. */
	public static final CharacterEntries NONE = new CharacterEntries(Map.of(), Map.of(), Map.of());

	public CharacterEntries {
		species = Map.copyOf(species);
		memberships = Map.copyOf(memberships);
		devilFruits = Map.copyOf(devilFruits);
	}

	/** Whether the reader knows the character to have the species, now or formerly. */
	public boolean hasSpecies(UUID characterId, UUID speciesId, ReaderSight sight) {
		return sight
			.discovered(this.species.getOrDefault(characterId, List.of()), SpeciesEntry::speciesId,
					SpeciesEntry::withWindow)
			.stream()
			.anyMatch((entry) -> entry.speciesId().equals(speciesId));
	}

	/** Whether the reader knows the character to belong to the affiliation now. */
	public boolean belongsTo(UUID characterId, UUID affiliationId, ReaderSight sight) {
		return currentMemberships(characterId, sight).anyMatch((m) -> m.affiliationId().equals(affiliationId));
	}

	/**
	 * Whether the reader knows the character to belong now to an affiliation of the kind.
	 */
	public boolean belongsToKind(UUID characterId, String kind, ReaderSight sight) {
		return currentMemberships(characterId, sight)
			.anyMatch((m) -> m.affiliation().kind() != null && m.affiliation().kind().name().equals(kind));
	}

	/** Whether the reader knows the character to have the Devil Fruit's power now. */
	public boolean hasDevilFruit(UUID characterId, UUID devilFruitId, ReaderSight sight) {
		return sight
			.discovered(this.devilFruits.getOrDefault(characterId, List.of()), FruitUse::devilFruitId,
					FruitUse::withWindow)
			.stream()
			.anyMatch((use) -> use.devilFruitId().equals(devilFruitId) && !use.former());
	}

	/** How many characters the reader has met belong now to the affiliation. */
	public long countMembers(UUID affiliationId, ReaderSight sight) {
		return this.memberships.keySet()
			.stream()
			.filter(sight::sees)
			.filter((characterId) -> belongsTo(characterId, affiliationId, sight))
			.count();
	}

	private Stream<Membership> currentMemberships(UUID characterId, ReaderSight sight) {
		return Membership.metBy(this.memberships.getOrDefault(characterId, List.of()), sight)
			.stream()
			.filter((membership) -> !membership.former());
	}

}
