package dev.onepieceapi.publicapi.persistence.row;

/**
 * A row holding a window (workflow 3.7.2) as the views give it: chapter and episode
 * numbers, each null when not on that axis.
 */
public interface WindowedRow {

	Integer discoveredChapter();

	Integer discoveredEpisode();

	Integer endedChapter();

	Integer endedEpisode();

}
