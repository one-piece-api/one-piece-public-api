package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.AffiliationKind;

import java.util.UUID;

/**
 * An affiliation of a character, inside the character.
 *
 * @param kind null for a version written before kinds existed
 */
public record AffiliationReferenceResponse(UUID id, String slug, String name, AffiliationKind kind) {

}
