package dev.onepieceapi.publicapi.web.dto.response;

/**
 * What the reader believes of a Devil Fruit at its first appearance (workflow 3.7.1):
 * only the fields some reveal names, the others {@code null}.
 *
 * @param name in the language of the path
 * @param slug the address of the romaji
 */
public record DevilFruitKnowledgeResponse(String name, String romaji, String slug, DevilFruitTypeReferenceResponse type,
		DevilFruitSubcategoryReferenceResponse subcategory) {

}
