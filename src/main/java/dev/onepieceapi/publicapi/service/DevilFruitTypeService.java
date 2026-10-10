package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.ContentLookup;
import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedSubcategoryRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The Devil Fruit Types online: the generic rules, bound to their view, and the detail
 * completed with the type's subcategories and its fruits online (plan D12, subcategories
 * plan S9).
 */
@Service
public class DevilFruitTypeService extends PublishedContentService<DevilFruitType, DevilFruitTypeSummary> {

	private final PublishedContentRepository repository;

	private final PublishedSubcategoryRepository subcategories;

	DevilFruitTypeService(LanguageService languages, PublishedContentRepository repository,
			PublishedSubcategoryRepository subcategories) {
		super(languages, repository, PublishedViews.DEVIL_FRUIT_TYPE);
		this.repository = repository;
		this.subcategories = subcategories;
	}

	@Override
	public ContentLookup<DevilFruitType> find(String language, String idOrSlug) {
		return switch (super.find(language, idOrSlug)) {
			case ContentLookup.Found<DevilFruitType> found -> new ContentLookup.Found<>(withRelations(found.content()));
			case ContentLookup.Moved<DevilFruitType> moved -> moved;
		};
	}

	private DevilFruitType withRelations(DevilFruitType type) {
		var ofThisType = new ContentFilterValue(PublishedViews.TYPE_FILTER, type.id().toString());
		return type.withSubcategories(this.subcategories.findOfType(type.id(), type.language()))
			.withDevilFruits(this.repository.listAll(PublishedViews.DEVIL_FRUIT, type.language(), ofThisType));
	}

}
