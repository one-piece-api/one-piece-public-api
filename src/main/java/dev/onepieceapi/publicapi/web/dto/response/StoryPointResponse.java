package dev.onepieceapi.publicapi.web.dto.response;

/**
 * A point on the reader axis (workflow 3.7): where the reader of the manga (chapter) or
 * of the anime (episode) learns something.
 *
 * @param chapter the chapter number; {@code null} when not seen on the manga axis
 * @param episode the episode number; {@code null} when not seen on the anime axis
 */
public record StoryPointResponse(Integer chapter, Integer episode) {

}
