package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * A Character as a row of the list.
 *
 * @param imageId the portrait's identifier; null when it has none
 */
@Builder(toBuilder = true)
public record CharacterSummary(UUID id, String slug, String romaji, String name, CharacterStatus status,
		String imageId) implements SeenByReader<CharacterSummary> {

	@Override
	public CharacterSummary seenBy(ReaderSight sight) {
		return this;
	}

}
