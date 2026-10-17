package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * A Devil Fruit Type as a row of the list.
 *
 * @param firstAppearance where the reader first meets it
 */
@Builder
public record DevilFruitTypeSummary(UUID id, String slug, String romaji, String name, FirstAppearance firstAppearance) {

}
