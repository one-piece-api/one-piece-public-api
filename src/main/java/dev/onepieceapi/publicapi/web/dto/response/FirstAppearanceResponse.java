package dev.onepieceapi.publicapi.web.dto.response;

/**
 * Where the reader first meets a content, one part per medium: only the manga's today, an
 * {@code episode} part may be added later.
 *
 * @param chapter the chapter of the first appearance
 */
public record FirstAppearanceResponse(ChapterReferenceResponse chapter) {

}
