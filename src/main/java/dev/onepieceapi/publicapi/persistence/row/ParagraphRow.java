package dev.onepieceapi.publicapi.persistence.row;

/**
 * A paragraph of a long text, read from {@code published.paragraph} in the language of
 * the path.
 *
 * @param section {@code DESCRIPTION}, {@code ADVANTAGES} or {@code DISADVANTAGES}
 */
public record ParagraphRow(String section, String text, Integer discoveredChapter, Integer discoveredEpisode,
		Integer endedChapter, Integer endedEpisode) implements WindowedRow {

}
