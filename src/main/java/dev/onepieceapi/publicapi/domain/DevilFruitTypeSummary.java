package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/** A Devil Fruit Type as a row of the list. */
public record DevilFruitTypeSummary(UUID id, String slug, String romaji, String name) {

}
