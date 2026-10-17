package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/** A chapter inside a content that points to it (a first appearance, a reveal). */
public record ChapterReferenceResponse(UUID id, String slug, int number, String title) {

}
