package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/** The columns of {@code published.chapter} a list row shows. */
public record ChapterSummaryRow(UUID id, String slug, int number, String name, UUID arcId, String arcSlug,
		int arcNumber, String arcTitle, UUID volumeId, String volumeSlug, Integer volumeNumber, String volumeTitle,
		String volumeCoverImageId) {

}
