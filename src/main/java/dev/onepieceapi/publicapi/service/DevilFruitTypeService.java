package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/** The Devil Fruit Types online: the generic rules, bound to their view. */
@Service
public class DevilFruitTypeService extends PublishedContentService<DevilFruitType, DevilFruitTypeSummary> {

	DevilFruitTypeService(LanguageService languages, PublishedContentRepository repository) {
		super(languages, repository, PublishedViews.DEVIL_FRUIT_TYPE);
	}

}
