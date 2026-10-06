package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/** The columns of {@code published.devil_fruit_type} a list row shows. */
public record DevilFruitTypeSummaryRow(UUID id, String slug, String romaji, String name) {

}
