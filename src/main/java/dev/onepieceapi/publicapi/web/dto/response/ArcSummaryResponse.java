package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/**
 * A row of the list of arcs.
 *
 * @param saga its saga, in the same language
 */
public record ArcSummaryResponse(UUID id, String slug, int number, String title, SagaSummaryResponse saga) {

}
