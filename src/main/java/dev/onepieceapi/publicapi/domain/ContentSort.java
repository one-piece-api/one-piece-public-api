package dev.onepieceapi.publicapi.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the list is ordered: a field of a closed set and a direction, so nothing a caller
 * sends ever reaches the SQL as text.
 */
public record ContentSort(Field field, Direction direction) {

	public static final ContentSort DEFAULT = new ContentSort(Field.NAME, Direction.ASC);

	/** The default of the entities keyed by a number: story order. */
	public static final ContentSort BY_NUMBER = new ContentSort(Field.NUMBER, Direction.ASC);

	/** The default of the bounties: the highest first (Bounty plan D5). */
	public static final ContentSort BY_AMOUNT_DESCENDING = new ContentSort(Field.AMOUNT, Direction.DESC);

	/**
	 * What the list can be ordered by; {@code requestName} is the name in the URL. A
	 * field that is not {@code common} orders only the lists of the entities that declare
	 * it.
	 */
	@Getter
	@RequiredArgsConstructor
	public enum Field {

		NAME("name", true), ROMAJI("romaji", true), NUMBER("number", true), PUBLISHED_AT("publishedAt", true),
		AMOUNT("amount", false);

		private final String requestName;

		private final boolean common;

	}

	public enum Direction {

		ASC, DESC

	}

}
