package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/** A row of the list of Devil Fruit Types. */
public record DevilFruitTypeSummaryResponse(UUID id, String slug, String romaji, String name) {

}
