package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * An entry of a character's list with no more than a list filter tests: the content it
 * names and its window, read from {@code published.character_species} or
 * {@code published.character_devil_fruit}.
 *
 * @param characterId the character the entry belongs to
 * @param id the species or the fruit named
 */
public record EntryRow(UUID characterId, UUID id, Integer discoveredChapter, Integer discoveredEpisode,
		Integer endedChapter, Integer endedEpisode) implements WindowedRow {

}
