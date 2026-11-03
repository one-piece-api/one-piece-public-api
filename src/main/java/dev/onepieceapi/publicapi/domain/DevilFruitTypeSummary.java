package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * A Devil Fruit Type as a row of the list.
 *
 * @param firstAppearance where the reader first meets it
 */
@Builder(toBuilder = true)
public record DevilFruitTypeSummary(UUID id, String slug, String romaji, String name,
		FirstAppearance firstAppearance) implements SeenByReader<DevilFruitTypeSummary> {

	@Override
	public DevilFruitTypeSummary seenBy(ReaderSight sight) {
		return toBuilder().firstAppearance(sight.onAxis(this.firstAppearance)).build();
	}

}
