package dev.onepieceapi.publicapi.persistence.row;

/**
 * An epithet or an alias of a character, read from {@code published.character_epithet} or
 * {@code published.character_alias}.
 */
public record TextEntryRow(String value, Integer discoveredChapter, Integer discoveredEpisode, Integer endedChapter,
		Integer endedEpisode) implements WindowedRow {

}
