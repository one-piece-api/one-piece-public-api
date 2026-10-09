package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.ContentLookup;
import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The Devil Fruit Types online: the generic rules, bound to their view, and the detail
 * completed with the type's fruits online (plan D12).
 */
@Service
public class DevilFruitTypeService extends PublishedContentService<DevilFruitType, DevilFruitTypeSummary> {

	private final PublishedContentRepository repository;

	DevilFruitTypeService(LanguageService languages, PublishedContentRepository repository) {
		super(languages, repository, PublishedViews.DEVIL_FRUIT_TYPE);
		this.repository = repository;
	}

	@Override
	public ContentLookup<DevilFruitType> find(String language, String idOrSlug) {
		return switch (super.find(language, idOrSlug)) {
			case ContentLookup.Found<DevilFruitType> found ->
				new ContentLookup.Found<>(withDevilFruits(found.content()));
			case ContentLookup.Moved<DevilFruitType> moved -> moved;
		};
	}

	private DevilFruitType withDevilFruits(DevilFruitType type) {
		var ofThisType = new ContentFilterValue(PublishedViews.TYPE_FILTER, type.id().toString());
		return type.withDevilFruits(this.repository.listAll(PublishedViews.DEVIL_FRUIT, type.language(), ofThisType));
	}

}
