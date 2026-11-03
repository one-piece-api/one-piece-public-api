package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A chapter as a row of the list.
 *
 * @param arc its arc, in the same language
 * @param volume its volume; null when it is not collected in one yet
 */
public record ChapterSummary(UUID id, String slug, int number, String title, ArcReference arc,
		VolumeSummary volume) implements SeenByReader<ChapterSummary> {

	@Override
	public ChapterSummary seenBy(ReaderSight sight) {
		return new ChapterSummary(this.id, this.slug, this.number, this.title, sight.ifSeen(this.arc, ArcReference::id),
				sight.ifSeen(this.volume, VolumeSummary::id));
	}

}
