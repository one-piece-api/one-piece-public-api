package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A row of {@code published.devil_fruit_type}. */
@Builder
public record DevilFruitTypeRow(UUID id, String slug, String romaji, String language, String name, String description,
		String advantages, String disadvantages, OffsetDateTime publishedAt) {

}
