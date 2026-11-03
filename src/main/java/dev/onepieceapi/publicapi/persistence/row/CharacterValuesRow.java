package dev.onepieceapi.publicapi.persistence.row;

/**
 * The revealable values of a character, read from
 * {@code published.character_initial_knowledge} or {@code published.character_reveal}
 * with the name in the language of the path; a value not held is null.
 *
 * @param fields the fields a reveal names, comma separated; null for the initial
 * knowledge
 * @param title the reveal's title; null for the initial knowledge
 * @param revealedChapter where a reveal is learned; null for the initial knowledge
 */
public record CharacterValuesRow(String fields, String title, String name, String romaji, String slug,
		String japaneseName, String status, Integer revealedChapter, Integer revealedEpisode) {

}
