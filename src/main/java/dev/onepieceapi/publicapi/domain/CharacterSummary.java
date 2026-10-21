package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * A Character as a row of the list.
 *
 * @param imageId the portrait's identifier; null when it has none
 */
@Builder
public record CharacterSummary(UUID id, String slug, String romaji, String name, CharacterStatus status,
		String imageId) {

}
