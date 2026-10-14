package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/**
 * A row of the list of chapters.
 *
 * @param arc its arc, in the same language
 * @param volume its volume; {@code null} when it is not collected in one yet
 */
public record ChapterSummaryResponse(UUID id, String slug, int number, String title, ArcReferenceResponse arc,
		VolumeSummaryResponse volume) {

}
