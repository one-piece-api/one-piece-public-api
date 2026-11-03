package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * An image of a character's gallery, read from {@code published.character_gallery} with
 * its caption in the language of the path.
 *
 * @param kind {@code ASPETTO} or {@code FORMA}
 * @param caption null when none is written in the language
 */
public record GalleryImageRow(UUID characterId, String imageId, String kind, String caption, Integer discoveredChapter,
		Integer discoveredEpisode, Integer endedChapter, Integer endedEpisode) implements WindowedRow {

}
