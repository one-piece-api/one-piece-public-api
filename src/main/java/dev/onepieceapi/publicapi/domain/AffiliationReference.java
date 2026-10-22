package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * An Affiliation inside a character that belongs to it.
 *
 * @param kind null for a version written before kinds existed
 */
public record AffiliationReference(UUID id, String slug, String name, AffiliationKind kind) {

}
