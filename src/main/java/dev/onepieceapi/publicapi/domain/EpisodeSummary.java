package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * An episode as a row of the list.
 *
 * @param arc its arc; null for an episode outside any arc (a filler)
 */
public record EpisodeSummary(UUID id, String slug, int number, String title,
		ArcReference arc) implements SeenByReader<EpisodeSummary> {

	@Override
	public EpisodeSummary seenBy(ReaderSight sight) {
		return new EpisodeSummary(this.id, this.slug, this.number, this.title,
				sight.ifSeen(this.arc, ArcReference::id));
	}

}
