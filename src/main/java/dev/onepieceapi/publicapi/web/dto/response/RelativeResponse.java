package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.RelativeKind;

/**
 * A relative of a character: what it is to the character, whichever of the two stores the
 * link.
 */
public record RelativeResponse(RelativeReferenceResponse character, RelativeKind kind) {

}
