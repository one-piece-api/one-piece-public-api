package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/**
 * An arc inside a content it relates to (a saga, a volume, a chapter, an episode): the
 * enclosing saga, if any, is left out.
 */
public record ArcReferenceResponse(UUID id, String slug, int number, String title) {

}
