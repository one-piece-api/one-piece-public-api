package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.ReaderPosition;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * What the reader may know of the revealable fields of every content online (workflow
 * 3.7.1, 3.8; reveals plan R7 S10): the initial knowledge and the reveals of each
 * Character, Devil Fruit and Bounty that has some, and which bounties replace which. Read
 * once per filtered request, as the meeting points, so every detail, row and link names a
 * content as known at N. A content without reveals is known by its current values.
 *
 * @param successors for each bounty, the bounties online that name it as previous
 */
@Builder
public record ReaderKnowledge(Map<UUID, FieldReveals<CharacterField, CharacterValues>> characters,
		Map<UUID, FieldReveals<DevilFruitField, DevilFruitValues>> devilFruits,
		Map<UUID, FieldReveals<BountyField, BountyValues>> bounties, Map<UUID, List<UUID>> successors) {

	public ReaderKnowledge {
		characters = (characters == null) ? Map.of() : Map.copyOf(characters);
		devilFruits = (devilFruits == null) ? Map.of() : Map.copyOf(devilFruits);
		bounties = (bounties == null) ? Map.of() : Map.copyOf(bounties);
		successors = (successors == null) ? Map.of() : Map.copyOf(successors);
	}

	/** Nothing revealed: every content is known by its current values. */
	public static ReaderKnowledge none() {
		return builder().build();
	}

	public CharacterValues characterAt(UUID id, CharacterValues current, ReaderPosition position) {
		return this.characters.getOrDefault(id, FieldReveals.none())
			.valuesAt(current, CharacterValues.UNKNOWN, position);
	}

	public DevilFruitValues devilFruitAt(UUID id, DevilFruitValues current, ReaderPosition position) {
		return this.devilFruits.getOrDefault(id, FieldReveals.none())
			.valuesAt(current, DevilFruitValues.UNKNOWN, position);
	}

	/**
	 * Whether the poster no longer holds as known at N; unknown counts as holding
	 * (reveals plan R7 step 4 (c), decision 3: the field defaults to false).
	 */
	public boolean inactiveAt(UUID id, boolean current, ReaderPosition position) {
		BountyValues known = this.bounties.getOrDefault(id, FieldReveals.none())
			.valuesAt(new BountyValues(current), BountyValues.UNKNOWN, position);
		return Boolean.TRUE.equals(known.inactive());
	}

	/** The bounties online that replace this one. */
	public List<UUID> successorsOf(UUID bountyId) {
		return this.successors.getOrDefault(bountyId, List.of());
	}

	/**
	 * The slugs of a content the reader has met by N (workflow 3.8; plan D13): from each
	 * romaji known by then; the current one only for a content whose romaji is not
	 * revealed.
	 */
	public Set<String> slugsKnownAt(UUID id, String current, ReaderPosition position) {
		if (this.characters.containsKey(id)) {
			return this.characters.get(id).knownAt(CharacterField.IDENTITY, CharacterValues::slug, current, position);
		}
		if (this.devilFruits.containsKey(id)) {
			return this.devilFruits.get(id).knownAt(DevilFruitField.ROMAJI, DevilFruitValues::slug, current, position);
		}
		return Stream.ofNullable(current).collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * The slug at N of a content: from its romaji as known at N; its current one when
	 * neither a Character nor a Devil Fruit with reveals.
	 */
	public String slugAt(UUID id, String current, ReaderPosition position) {
		if (this.characters.containsKey(id)) {
			return characterAt(id, CharacterValues.UNKNOWN.withSlug(current), position).slug();
		}
		if (this.devilFruits.containsKey(id)) {
			return devilFruitAt(id, DevilFruitValues.UNKNOWN.withSlug(current), position).slug();
		}
		return current;
	}

}
