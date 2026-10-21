package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/** A character, inside the content that names it. */
public record CharacterReferenceResponse(UUID id, String slug, String name) {

}
