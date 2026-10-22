package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * A fruit of a character or a user of a fruit, read from
 * {@code published.character_devil_fruit} with the other side in the language of the
 * path.
 */
public record FruitUseRow(UUID id, String slug, String name, boolean former) {

}
