package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.DevilFruit;
import dev.onepieceapi.publicapi.domain.DevilFruitSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/** The Devil Fruits online: the generic rules, bound to their view. */
@Service
public class DevilFruitService extends PublishedContentService<DevilFruit, DevilFruitSummary> {

	DevilFruitService(LanguageService languages, PublishedContentRepository repository) {
		super(languages, repository, PublishedViews.DEVIL_FRUIT);
	}

}
