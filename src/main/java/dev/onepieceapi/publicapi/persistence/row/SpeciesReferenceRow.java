package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/** A species of a character, read from {@code published.species} in its language. */
public record SpeciesReferenceRow(UUID id, String slug, String name) {

}
