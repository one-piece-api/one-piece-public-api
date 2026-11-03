package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.GalleryImageKind;

/**
 * An image of a character's gallery, inside its window (workflow 3.5.1, 3.7.2).
 *
 * @param image the path relative to the API root
 * @param kind {@code APPEARANCE} may be the main portrait, {@code FORM} never
 * @param caption in the language of the path; {@code null} when none is written in it
 * @param former a past appearance
 * @param discovered where the reader learns it
 * @param ended where the reader learns it is over; {@code null} for none
 */
public record GalleryImageResponse(String image, GalleryImageKind kind, String caption, boolean former,
		StoryPointResponse discovered, StoryPointResponse ended) {

}
