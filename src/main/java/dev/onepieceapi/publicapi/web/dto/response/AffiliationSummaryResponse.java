package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.AffiliationKind;
import lombok.Builder;

import java.util.UUID;

/**
 * A row of the list of affiliations.
 *
 * @param kind null for a version written before kinds existed
 * @param image the emblem's path relative to the API root; {@code null} when none
 */
@Builder
public record AffiliationSummaryResponse(UUID id, String slug, String romaji, String name, AffiliationKind kind,
		String image) {

}
