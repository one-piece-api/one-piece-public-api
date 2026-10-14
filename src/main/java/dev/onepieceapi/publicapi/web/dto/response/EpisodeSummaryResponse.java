package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/**
 * A row of the list of episodes.
 *
 * @param arc its arc; {@code null} for an episode outside any arc (a filler)
 */
public record EpisodeSummaryResponse(UUID id, String slug, int number, String title, ArcReferenceResponse arc) {

}
