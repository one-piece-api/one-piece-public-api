package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.util.UUID;

/**
 * A row of the list of Devil Fruit Types.
 *
 * @param firstAppearance where the reader first meets it
 */
@Builder
public record DevilFruitTypeSummaryResponse(UUID id, String slug, String romaji, String name,
		FirstAppearanceResponse firstAppearance) {

}
