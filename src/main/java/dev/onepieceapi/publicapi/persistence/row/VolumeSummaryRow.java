package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/** The columns of {@code published.volume} a list row shows. */
public record VolumeSummaryRow(UUID id, String slug, int number, String name, String coverImageId) {

}
