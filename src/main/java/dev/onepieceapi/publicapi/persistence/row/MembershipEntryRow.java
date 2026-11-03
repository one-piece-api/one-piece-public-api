package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * A period of a membership with no more than a list filter tests: the affiliation, its
 * kind and the window of the period.
 *
 * @param characterId the character the membership belongs to
 * @param membershipPosition the membership the period belongs to
 * @param id the affiliation
 * @param kind the affiliation's kind; null for a version written before kinds existed
 */
public record MembershipEntryRow(UUID characterId, int membershipPosition, UUID id, String kind,
		Integer discoveredChapter, Integer discoveredEpisode, Integer endedChapter,
		Integer endedEpisode) implements WindowedRow {

}
