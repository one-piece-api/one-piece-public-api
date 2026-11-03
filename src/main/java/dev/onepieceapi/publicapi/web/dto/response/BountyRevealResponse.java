package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.BountyField;

import java.util.List;

/**
 * A point where the reader learns that a bounty no longer holds, or holds again (workflow
 * 3.7.1).
 *
 * @param title in the language of the path
 * @param revealed where the reader learns
 * @param inactive the value learned, the current one included
 */
public record BountyRevealResponse(String title, StoryPointResponse revealed, List<BountyField> fields,
		Boolean inactive) {

}
