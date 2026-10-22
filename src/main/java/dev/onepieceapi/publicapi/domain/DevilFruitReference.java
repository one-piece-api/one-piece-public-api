package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/** A Devil Fruit inside a character that names it. */
public record DevilFruitReference(UUID id, String slug, String name) {

}
