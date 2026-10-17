package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A subcategory of a Devil Fruit Type online, in one language (subcategories plan S9).
 *
 * @param firstAppearance where the reader first meets it; null when not given
 */
public record DevilFruitSubcategory(UUID id, String name, String description, FirstAppearance firstAppearance) {

}
