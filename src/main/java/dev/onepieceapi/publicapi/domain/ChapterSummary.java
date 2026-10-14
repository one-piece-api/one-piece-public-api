package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A chapter as a row of the list.
 *
 * @param arc its arc, in the same language
 * @param volume its volume; null when it is not collected in one yet
 */
public record ChapterSummary(UUID id, String slug, int number, String title, ArcReference arc, VolumeSummary volume) {

}
