package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/** A saga as a row of the list, or as the saga of an arc. */
public record SagaSummary(UUID id, String slug, int number, String title) {

}
