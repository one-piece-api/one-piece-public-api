package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/** The columns of {@code published.episode} a list row shows. */
public record EpisodeSummaryRow(UUID id, String slug, int number, String name, UUID arcId, String arcSlug,
		Integer arcNumber, String arcTitle) {

}
