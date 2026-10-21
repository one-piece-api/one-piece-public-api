package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * An Affiliation as a row of the list.
 *
 * @param kind null for a version written before kinds existed
 * @param imageId the emblem's identifier; null when it has none
 */
@Builder
public record AffiliationSummary(UUID id, String slug, String romaji, String name, AffiliationKind kind,
		String imageId) {

}
