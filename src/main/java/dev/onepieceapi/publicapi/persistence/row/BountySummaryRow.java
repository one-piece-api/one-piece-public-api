package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/** The columns of {@code published.bounty} a list row or a link of a chain shows. */
public record BountySummaryRow(UUID id, String slug, String posterName, long amount, String condition, String validity,
		String imageId) {

}
