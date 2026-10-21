package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.util.UUID;

/** The columns of {@code published.affiliation} a list row shows. */
@Builder
public record AffiliationSummaryRow(UUID id, String slug, String romaji, String name, String kind, String imageId) {

}
