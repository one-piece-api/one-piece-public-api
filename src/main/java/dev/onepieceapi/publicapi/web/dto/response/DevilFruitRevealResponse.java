package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.DevilFruitField;

import java.util.List;

/**
 * A point where the reader learns some fields of a Devil Fruit (workflow 3.7.1). Each
 * field named carries its value, the current one included; the others are {@code null}.
 *
 * @param title in the language of the path
 * @param revealed where the reader learns
 * @param fields the fields learned, {@code TYPE} being the type and the subcategory
 * @param name in the language of the path
 * @param slug the address of the romaji
 */
public record DevilFruitRevealResponse(String title, StoryPointResponse revealed, List<DevilFruitField> fields,
		String name, String romaji, String slug, DevilFruitTypeReferenceResponse type,
		DevilFruitSubcategoryReferenceResponse subcategory) {

}
