package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/**
 * A row of the list of volumes, or the volume of a chapter.
 *
 * @param cover the cover image path relative to the API root; {@code null} when none
 */
public record VolumeSummaryResponse(UUID id, String slug, int number, String title, String cover) {

}
