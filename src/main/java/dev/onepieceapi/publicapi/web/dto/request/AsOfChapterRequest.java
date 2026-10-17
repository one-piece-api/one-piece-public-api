package dev.onepieceapi.publicapi.web.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;

/**
 * The spoiler filter of a resource (plan of the timeline, T8), on its list and its
 * detail.
 *
 * @param asOfChapter the last chapter the reader has read, inclusive; nothing is hidden
 * when missing
 */
public record AsOfChapterRequest(@Parameter(description = "Spoiler filter: the last chapter read, inclusive."
		+ " What the reader meets after it is left out of lists, searches and counts, and its detail answers 404."
		+ " Missing: nothing is hidden") @Min(0) Integer asOfChapter) {

}
