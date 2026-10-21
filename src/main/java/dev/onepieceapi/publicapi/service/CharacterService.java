package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Character;
import dev.onepieceapi.publicapi.domain.CharacterSummary;
import dev.onepieceapi.publicapi.persistence.PublishedCharacterListRepository;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The characters online: the generic rules, bound to their view, and the detail completed
 * with its epithets, aliases and species (Character plan C2).
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
			.withSpecies(this.lists.findSpecies(character.id(), character.language()));
	}

}
