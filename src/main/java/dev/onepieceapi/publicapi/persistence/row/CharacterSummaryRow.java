package dev.onepieceapi.publicapi.persistence.row;

import lombok.Builder;

import java.util.UUID;

/** The columns of {@code published.character} a list row shows. */
@Builder
public record CharacterSummaryRow(UUID id, String slug, String romaji, String name, String status, String imageId) {

}
