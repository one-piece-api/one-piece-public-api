package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/** An arc as the arc of a chapter or an episode: one level up, no saga. */
public record ArcReference(UUID id, String slug, int number, String title) {

}
