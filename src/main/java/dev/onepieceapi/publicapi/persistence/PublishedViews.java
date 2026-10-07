package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.persistence.mapper.PublishedRowMapper;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeSummaryRow;

/**
 * The entities online, one descriptor each. An explicit private constructor, as in
 * {@code ApiPaths}: checkstyle does not see the one Lombok would add.
 */
public final class PublishedViews {

	public static final PublishedView<DevilFruitType, DevilFruitTypeSummary> DEVIL_FRUIT_TYPE = PublishedView
		.<DevilFruitType, DevilFruitTypeSummary>builder()
		.entityType("DEVIL_FRUIT_TYPE")
		.view("published.devil_fruit_type")
		.detailColumns("id, slug, romaji, language, name, description, advantages, disadvantages, published_at")
		.summaryColumns("id, slug, romaji, name")
		.detail(PublishedView.reading(DevilFruitTypeRow.class, PublishedRowMapper::toDomain))
		.summary(PublishedView.reading(DevilFruitTypeSummaryRow.class, PublishedRowMapper::toDomain))
		.build();

	private PublishedViews() {
	}

}
