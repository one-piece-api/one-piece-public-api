package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.DevilFruitSummary;
import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.NameOrder;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedSubcategoryRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * The Devil Fruit Types online: the generic rules, bound to their view, and the detail
 * completed with the type's subcategories and its fruits online (plan D12, subcategories
 * plan S9).
 */
@Service
public class DevilFruitTypeService extends PublishedContentService<DevilFruitType, DevilFruitTypeSummary> {

	private final PublishedContentRepository repository;

	private final PublishedSubcategoryRepository subcategories;

	DevilFruitTypeService(LanguageService languages, PublishedContentRepository repository, ReaderSightService sights,
			PublishedSubcategoryRepository subcategories) {
		super(languages, repository, sights, PublishedViews.DEVIL_FRUIT_TYPE);
		this.repository = repository;
		this.subcategories = subcategories;
	}

	@Override
	protected DevilFruitType complete(DevilFruitType type) {
		var ofThisType = new ContentFilterValue(PublishedViews.TYPE_FILTER, type.id().toString());
		return withSubcategories(type)
			.withDevilFruits(this.repository.listAll(PublishedViews.DEVIL_FRUIT, type.language(), ofThisType));
	}

	/**
	 * Its fruits are those whose type at N is this one, by name at N (reveals plan R7
	 * S11): a fruit may have been typed otherwise before a reveal.
	 */
	@Override
	protected DevilFruitType completeFor(DevilFruitType type, ReaderSight sight) {
		return withSubcategories(type).withDevilFruits(fruitsOfTypeAt(type, sight)).seenBy(sight);
	}

	private DevilFruitType withSubcategories(DevilFruitType type) {
		return type.withSubcategories(this.subcategories.findOfType(type.id(), type.language()));
	}

	private List<DevilFruitSummary> fruitsOfTypeAt(DevilFruitType type, ReaderSight sight) {
		Comparator<DevilFruitSummary> byNameAtN = Comparator
			.comparing((DevilFruitSummary fruit) -> fruit.seenBy(sight).name(), NameOrder.in(type.language()))
			.thenComparing(DevilFruitSummary::id);
		return this.repository.listEvery(PublishedViews.DEVIL_FRUIT, type.language())
			.stream()
			.filter((fruit) -> sight.sees(fruit.id()) && isOfTypeAt(fruit.seenBy(sight), type))
			.sorted(byNameAtN)
			.toList();
	}

	private static boolean isOfTypeAt(DevilFruitSummary fruitAtN, DevilFruitType type) {
		return fruitAtN.type() != null && fruitAtN.type().id().equals(type.id());
	}

}
