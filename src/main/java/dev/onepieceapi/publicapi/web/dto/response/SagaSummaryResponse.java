package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/** A row of the list of sagas, or the saga of an arc. */
public record SagaSummaryResponse(UUID id, String slug, int number, String title) {

}
