package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * An Affiliation as a row of the list.
 *
 * @param imageId the emblem's identifier; null when it has none
 */
@Builder
public record AffiliationSummary(UUID id, String slug, String romaji, String name, String imageId) {

}
