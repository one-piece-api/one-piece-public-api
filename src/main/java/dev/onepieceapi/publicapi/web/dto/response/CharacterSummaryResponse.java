package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.CharacterStatus;
import lombok.Builder;

import java.util.UUID;

/**
 * A row of the list of characters.
 *
 * @param image the portrait's path relative to the API root; {@code null} when none
 */
@Builder
public record CharacterSummaryResponse(UUID id, String slug, String romaji, String name, CharacterStatus status,
		String image) {

}
