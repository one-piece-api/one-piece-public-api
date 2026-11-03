package dev.onepieceapi.publicapi.persistence.row;

/**
 * The revealable values of a bounty, read from {@code published.bounty_initial_knowledge}
 * or {@code published.bounty_reveal} with the title in the language of the path.
 *
 * @param fields the fields a reveal names, comma separated; null for the initial
 * knowledge
 * @param title the reveal's title; null for the initial knowledge
 * @param inactive null when not held
 * @param revealedChapter where a reveal is learned; null for the initial knowledge
 */
public record BountyValuesRow(String fields, String title, Boolean inactive, Integer revealedChapter,
		Integer revealedEpisode) {

}
