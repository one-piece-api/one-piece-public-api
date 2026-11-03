package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * An arc as a row of the list.
 *
 * @param saga its saga, in the same language
 */
public record ArcSummary(UUID id, String slug, int number, String title,
		SagaSummary saga) implements SeenByReader<ArcSummary> {

	@Override
	public ArcSummary seenBy(ReaderSight sight) {
		return new ArcSummary(this.id, this.slug, this.number, this.title, sight.ifSeen(this.saga, SagaSummary::id));
	}

}
