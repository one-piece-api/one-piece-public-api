package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * An Species as a row of the list.
 *
 * @param imageId the picture's identifier; null when it has none
 */
@Builder(toBuilder = true)
public record SpeciesSummary(UUID id, String slug, String romaji, String name,
		String imageId) implements SeenByReader<SpeciesSummary> {

	@Override
	public SpeciesSummary seenBy(ReaderSight sight) {
		return this;
	}

}
