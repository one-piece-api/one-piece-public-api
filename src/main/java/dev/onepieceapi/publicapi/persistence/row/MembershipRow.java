package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * A period of a membership of a character, read from
 * {@code published.character_affiliation_period} with the affiliation and the role in the
 * language of the path.
 *
 * @param membershipPosition the membership the period belongs to
 */
public record MembershipRow(int membershipPosition, UUID id, String slug, String name, String kind, String role,
		String marineRank, Integer discoveredChapter, Integer discoveredEpisode, Integer endedChapter,
		Integer endedEpisode) implements WindowedRow {

}
