package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * A relative of a character, read from {@code published.character_relative} with the
 * other character in the language of the path.
 */
public record RelativeRow(UUID id, String slug, String name, String gender, String kind) {

}
