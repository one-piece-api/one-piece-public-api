package dev.onepieceapi.publicapi.web.dto.response;

/**
 * Where the reader first meets a content, one part per medium; at least one is there.
 *
 * @param chapter the chapter, {@code null} for none
 * @param episode the episode, {@code null} for none
 */
public record FirstAppearanceResponse(StoryItemReferenceResponse chapter, StoryItemReferenceResponse episode) {

}
