package dev.onepieceapi.publicapi.web.dto.request;

import dev.onepieceapi.publicapi.web.validation.AtMostOneReaderAxis;
import dev.onepieceapi.readertime.ReaderPosition;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Positive;

/**
 * The spoiler filter of every resource (workflow 3.8; reveals plan D12): the reader is at
 * chapter N or at episode N, inclusive. Neither: nothing is hidden, the current values.
 *
 * @param asOfChapter the last chapter read, 1 or more; not with {@code asOfEpisode}
 * @param asOfEpisode the last episode watched, 1 or more; not with {@code asOfChapter}
 */
@AtMostOneReaderAxis
public record ReaderAxisRequest(@Parameter(description = ReaderAxisRequest.CHAPTER) @Positive Integer asOfChapter,
		@Parameter(description = ReaderAxisRequest.EPISODE) @Positive Integer asOfEpisode) {

	static final String CHAPTER = "Spoiler filter: the last chapter read, inclusive, 1 or more; not with asOfEpisode."
			+ " What the reader meets after it is left out of lists, searches and counts, its detail answers 404,"
			+ " a link to it is null; episodes are hidden. Missing: nothing is hidden";

	static final String EPISODE = "Spoiler filter: the last episode watched, inclusive, 1 or more; not with"
			+ " asOfChapter. As asOfChapter, on the anime: chapters and volumes are hidden. Missing: nothing is hidden";

	/** Where the reader is; null when nothing is hidden. */
	public ReaderPosition position() {
		if (this.asOfChapter != null) {
			return ReaderPosition.atChapter(this.asOfChapter);
		}
		return (this.asOfEpisode != null) ? ReaderPosition.atEpisode(this.asOfEpisode) : null;
	}

}
