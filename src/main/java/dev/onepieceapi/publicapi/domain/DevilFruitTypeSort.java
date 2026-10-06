package dev.onepieceapi.publicapi.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the list is ordered: a field of a closed set and a direction, so nothing a caller
 * sends ever reaches the SQL as text.
 */
public record DevilFruitTypeSort(Field field, Direction direction) {

	public static final DevilFruitTypeSort DEFAULT = new DevilFruitTypeSort(Field.NAME, Direction.ASC);

	/** What the list can be ordered by; {@code requestName} is the name in the URL. */
	@Getter
	@RequiredArgsConstructor
	public enum Field {

		NAME("name"), ROMAJI("romaji"), PUBLISHED_AT("publishedAt");

		private final String requestName;

	}

	public enum Direction {

		ASC, DESC

	}

}
