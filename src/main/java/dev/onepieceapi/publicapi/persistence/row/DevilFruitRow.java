package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.devil_fruit}. */
@Builder
public record DevilFruitRow(UUID id, String slug, String romaji, String language, String name, String description,
		String advantages, String disadvantages, UUID typeId, String typeSlug, String typeRomaji, String typeName,
		String imageId, OffsetDateTime publishedAt) {

}
