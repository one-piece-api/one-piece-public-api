package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * A Devil Fruit Type in one language. Every field is always present, {@code null} when
 * missing (plan D7); nothing of the editorial workflow shows.
 */
@Builder
public record DevilFruitTypeResponse(UUID id, String slug, String romaji, String language, String name,
		String description, String advantages, String disadvantages, Instant publishedAt) {

}
