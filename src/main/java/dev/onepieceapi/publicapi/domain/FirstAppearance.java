package dev.onepieceapi.publicapi.domain;

/**
 * Where the reader first meets a content: a chapter (manga), an episode (anime) or both
 * (content-service ADR-0008).
 *
 * @param chapter {@code null} for none
 * @param episode {@code null} for none
 */
public record FirstAppearance(StoryItemReference chapter, StoryItemReference episode) {

	/** {@code null} when neither part is there. */
	public static FirstAppearance of(StoryItemReference chapter, StoryItemReference episode) {
		return chapter == null && episode == null ? null : new FirstAppearance(chapter, episode);
	}

}
