package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/** A chapter or an episode inside a content that points to it (a first appearance). */
public record StoryItemReference(UUID id, String slug, int number, String title) {

	/** {@code null} where the view has none: not chosen, or no text in the language. */
	public static StoryItemReference of(UUID id, String slug, Integer number, String title) {
		return id == null || number == null ? null : new StoryItemReference(id, slug, number, title);
	}

}
