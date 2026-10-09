package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.DevilFruit;
import dev.onepieceapi.publicapi.domain.DevilFruitSummary;
import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.persistence.mapper.PublishedRowMapper;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeSummaryRow;

import java.util.Map;

/**
 * The entities online, one descriptor each. An explicit private constructor, as in
 * {@code ApiPaths}: checkstyle does not see the one Lombok would add.
 */
public final class PublishedViews {

	/** The name of the filter of the fruits by their type (plan D11). */
	public static final String TYPE_FILTER = "type";

	public static final PublishedView<DevilFruitType, DevilFruitTypeSummary> DEVIL_FRUIT_TYPE = PublishedView
		.<DevilFruitType, DevilFruitTypeSummary>builder()
		.entityType("DEVIL_FRUIT_TYPE")
		.view("published.devil_fruit_type")
		.detailColumns("id, slug, romaji, language, name, description, advantages, disadvantages, published_at")
		.summaryColumns("id, slug, romaji, name")
		.detail(PublishedView.reading(DevilFruitTypeRow.class, PublishedRowMapper::toDomain))
		.summary(PublishedView.reading(DevilFruitTypeSummaryRow.class, PublishedRowMapper::toDomain))
		.build();

	public static final PublishedView<DevilFruit, DevilFruitSummary> DEVIL_FRUIT = PublishedView
		.<DevilFruit, DevilFruitSummary>builder()
		.entityType("DEVIL_FRUIT")
		.view("published.devil_fruit")
		.detailColumns("id, slug, romaji, language, name, description, advantages, disadvantages,"
				+ " type_id, type_slug, type_romaji, type_name, image_id, published_at")
		.summaryColumns("id, slug, romaji, name, type_id, type_slug, type_romaji, type_name, image_id")
		.detail(PublishedView.reading(DevilFruitRow.class, PublishedRowMapper::toDomain))
		.summary(PublishedView.reading(DevilFruitSummaryRow.class, PublishedRowMapper::toDomain))
		.filters(Map.of(TYPE_FILTER, ContentFilter.onContent("type_id", "DEVIL_FRUIT_TYPE")))
		.build();

	private PublishedViews() {
	}

}
