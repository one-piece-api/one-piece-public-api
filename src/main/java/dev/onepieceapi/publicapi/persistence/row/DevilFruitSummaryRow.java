package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.util.UUID;

/** The columns of {@code published.devil_fruit} a list row shows. */
@Builder
public record DevilFruitSummaryRow(UUID id, String slug, String romaji, String name, UUID typeId, String typeSlug,
		String typeRomaji, String typeName, String imageId, UUID subcategoryId, String subcategoryName) {

}
