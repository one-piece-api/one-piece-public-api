package dev.onepieceapi.publicapi.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * A Devil Fruit as a row of the list.
 *
 * @param type its type, in the same language
 * @param subcategory the subcategory of its type it names; null when none
 * @param imageId the image's identifier; null when the fruit has none
 */
@Builder
public record DevilFruitSummary(UUID id, String slug, String romaji, String name, DevilFruitTypeSummary type,
		DevilFruitSubcategorySummary subcategory, String imageId) {

}
