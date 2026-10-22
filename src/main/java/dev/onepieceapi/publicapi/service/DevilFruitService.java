package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.DevilFruit;
import dev.onepieceapi.publicapi.domain.DevilFruitSummary;
import dev.onepieceapi.publicapi.persistence.PublishedCharacterListRepository;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The Devil Fruits online: the generic rules, bound to their view, and the detail
 * completed with the characters that have eaten it (Character plan C10).
 */
@Service
public class DevilFruitService extends PublishedContentService<DevilFruit, DevilFruitSummary> {

	private final PublishedCharacterListRepository characters;

	DevilFruitService(LanguageService languages, PublishedContentRepository repository,
			PublishedCharacterListRepository characters) {
		super(languages, repository, PublishedViews.DEVIL_FRUIT);
		this.characters = characters;
	}

	@Override
	protected DevilFruit complete(DevilFruit devilFruit) {
		return devilFruit.withUsers(this.characters.findUsers(devilFruit.id(), devilFruit.language()));
	}

}
