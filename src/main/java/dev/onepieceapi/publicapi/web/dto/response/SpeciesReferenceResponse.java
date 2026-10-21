package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/** A species of a character, inside the character. */
public record SpeciesReferenceResponse(UUID id, String slug, String name) {

}
