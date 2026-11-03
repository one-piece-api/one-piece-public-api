package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * A species of a character, read from {@code published.species} in its language, with the
 * window of {@code published.character_species}.
 */
public record SpeciesReferenceRow(UUID id, String slug, String name, Integer discoveredChapter,
		Integer discoveredEpisode, Integer endedChapter, Integer endedEpisode) implements WindowedRow {

}
