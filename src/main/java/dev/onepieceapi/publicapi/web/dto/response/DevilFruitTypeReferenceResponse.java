package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/** The type of a Devil Fruit, inside the fruit. */
public record DevilFruitTypeReferenceResponse(UUID id, String slug, String romaji, String name) {

}
