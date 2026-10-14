package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A volume as a row of the list, or as the volume of a chapter.
 *
 * @param coverImageId the cover's identifier; null when the volume has none
 */
public record VolumeSummary(UUID id, String slug, int number, String title, String coverImageId) {

}
