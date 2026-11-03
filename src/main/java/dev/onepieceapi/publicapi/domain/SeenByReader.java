package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A content, or a list row of one, that can be shown as a reader at N sees it (reveals
 * plan R7 D1): its links to contents not met yet removed, its points trimmed to the
 * reader's axis. Whether the content itself is seen is the caller's test, on
 * {@link #id()}.
 *
 * @param <T> the record itself
 */
public interface SeenByReader<T> {

	UUID id();

	/** This record as the reader sees it; never null, the content being seen. */
	T seenBy(ReaderSight sight);

}
