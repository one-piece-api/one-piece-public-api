package dev.onepieceapi.publicapi.web.dto.response;

import lombok.Builder;

import java.util.UUID;

/**
 * A row of the list of species.
 *
 * @param image the picture's path relative to the API root; {@code null} when none
 */
@Builder
public record SpeciesSummaryResponse(UUID id, String slug, String romaji, String name, String image) {

}
