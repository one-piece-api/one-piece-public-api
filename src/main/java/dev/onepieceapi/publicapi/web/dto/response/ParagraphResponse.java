package dev.onepieceapi.publicapi.web.dto.response;

/**
 * A paragraph of a long text (workflow 3.7.3). An ended paragraph is never shown: its
 * text is superseded.
 *
 * @param text in the language of the path
 * @param discovered where the reader learns it
 */
public record ParagraphResponse(String text, StoryPointResponse discovered) {

}
