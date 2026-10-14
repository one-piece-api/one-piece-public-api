package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/** The columns of {@code published.saga} a list row shows. */
public record SagaSummaryRow(UUID id, String slug, int number, String name) {

}
