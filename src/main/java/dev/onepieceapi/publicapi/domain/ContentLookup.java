package dev.onepieceapi.publicapi.domain;

/**
 * The answer to "which content is this address": the content itself, or the current
 * address of the one an old slug used to name (plan D5).
 *
 * @param <T> the detail of the entity
 */
public sealed interface ContentLookup<T> {

	record Found<T>(T content) implements ContentLookup<T> {

	}

	record Moved<T>(String currentSlug) implements ContentLookup<T> {

	}

}
