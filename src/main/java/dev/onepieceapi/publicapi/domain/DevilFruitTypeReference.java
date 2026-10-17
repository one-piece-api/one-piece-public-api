package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/** A Devil Fruit Type inside a fruit that names it. */
public record DevilFruitTypeReference(UUID id, String slug, String romaji, String name) {

}
