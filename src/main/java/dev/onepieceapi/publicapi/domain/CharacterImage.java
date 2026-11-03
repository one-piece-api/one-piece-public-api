package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.GalleryImage;
import dev.onepieceapi.readertime.ImageKind;
import dev.onepieceapi.readertime.RevealWindow;

/**
 * An image of a character's gallery, inside its window (workflow 3.5.1): a former one is
 * a past appearance.
 *
 * @param imageId the image's identifier
 * @param caption in the reader's language; null when none is written in it
 */
public record CharacterImage(String imageId, ImageKind kind, String caption,
		RevealWindow window) implements GalleryImage, WindowedEntry {

}
