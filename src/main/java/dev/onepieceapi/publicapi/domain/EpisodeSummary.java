package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * An episode as a row of the list.
 *
 * @param arc its arc; null for an episode outside any arc (a filler)
 */
public record EpisodeSummary(UUID id, String slug, int number, String title, ArcReference arc) {

}
