package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/** A chapter inside a content that points to it (a first appearance, a reveal). */
public record ChapterReference(UUID id, String slug, int number, String title) {

}
