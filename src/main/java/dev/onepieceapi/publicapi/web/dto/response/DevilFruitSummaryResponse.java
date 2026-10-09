package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.util.UUID;

/**
 * A row of the list of Devil Fruits.
 *
 * @param type its type, in the same language
 * @param image the image's path relative to the API root; {@code null} when none
 */
@Builder
public record DevilFruitSummaryResponse(UUID id, String slug, String romaji, String name,
		DevilFruitTypeSummaryResponse type, String image) {

}
