package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.util.UUID;

/** The columns of {@code published.species} a list row shows. */
@Builder
public record SpeciesSummaryRow(UUID id, String slug, String romaji, String name, String imageId) {

}
