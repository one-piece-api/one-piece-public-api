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
public record AsOfChapterRequest(@Parameter(description = AsOfChapterRequest.DESCRIPTION) @Min(0) Integer asOfChapter) {

	static final String DESCRIPTION = "Spoiler filter: the last chapter read, inclusive."
			+ " What the reader meets after it is left out of lists, searches and counts, and its detail answers 404."
			+ " What is shown reads as known then: a value or a relation not known yet is null,"
			+ " searches, filters and sorting use the values known then. Missing: nothing is hidden, the truth";

}
