package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/** A Devil Fruit, inside the character that has eaten it. */
public record DevilFruitReferenceResponse(UUID id, String slug, String name) {

}
