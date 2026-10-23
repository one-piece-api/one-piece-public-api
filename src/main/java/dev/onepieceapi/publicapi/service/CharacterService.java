package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Character;
import dev.onepieceapi.publicapi.domain.CharacterSummary;
import dev.onepieceapi.publicapi.domain.Relative;
import dev.onepieceapi.publicapi.persistence.PublishedCharacterListRepository;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

/**
 * The characters online: the generic rules, bound to their view, and the detail completed
 * with its epithets, aliases, species, memberships, Devil Fruits, relatives and bounties
 * (Character plan C2, C6, C8, C10, C12).
 */
@Service
public class CharacterService extends PublishedContentService<Character, CharacterSummary> {

	private final PublishedCharacterListRepository lists;

	CharacterService(LanguageService languages, PublishedContentRepository repository,
			PublishedCharacterListRepository lists) {
		super(languages, repository, PublishedViews.CHARACTER);
		this.lists = lists;
	}

	@Override
	protected Character complete(Character character) {
		return character.withEpithets(this.lists.findEpithets(character.id(), character.language()))
			.withAliases(this.lists.findAliases(character.id()))
			.withSpecies(this.lists.findSpecies(character.id(), character.language()))
			.withAffiliations(this.lists.findAffiliations(character.id(), character.language()))
			.withDevilFruits(this.lists.findDevilFruits(character.id(), character.language()))
			.withRelatives(relativesOf(character))
			.withBounties(this.lists.findBounties(character.id(), character.language()));
	}

	/**
	 * The relatives it stores, then the characters online that store it, each through the
	 * inverse kind (Character plan D13).
	 */
	private List<Relative> relativesOf(Character character) {
		Stream<Relative> stored = this.lists.findRelatives(character.id(), character.language()).stream();
		Stream<Relative> derived = this.lists.findNamingAsRelative(character.id(), character.language())
			.stream()
			.map(Relative::inverted);
		return Stream.concat(stored, derived).toList();
	}

}
