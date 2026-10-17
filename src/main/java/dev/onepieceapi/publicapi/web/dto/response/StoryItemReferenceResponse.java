package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/** A chapter or an episode inside a content that points to it (a first appearance). */
public record StoryItemReferenceResponse(UUID id, String slug, int number, String title) {

}
