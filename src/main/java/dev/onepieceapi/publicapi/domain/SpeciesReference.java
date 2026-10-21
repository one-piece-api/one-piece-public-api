package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/** A Species inside a character that names it. */
public record SpeciesReference(UUID id, String slug, String name) {

}
