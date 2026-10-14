package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/** The columns of {@code published.arc} a list row shows. */
public record ArcSummaryRow(UUID id, String slug, int number, String name, UUID sagaId, String sagaSlug, int sagaNumber,
		String sagaTitle) {

}
