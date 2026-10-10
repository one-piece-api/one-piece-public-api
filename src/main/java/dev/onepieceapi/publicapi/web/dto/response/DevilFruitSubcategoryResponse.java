package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/** A subcategory of a Devil Fruit Type, in the language of the path. */
public record DevilFruitSubcategoryResponse(UUID id, String name, String description) {

}
