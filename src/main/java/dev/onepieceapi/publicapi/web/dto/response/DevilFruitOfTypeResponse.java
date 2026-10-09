package dev.onepieceapi.publicapi.web.dto.response;

import java.util.UUID;

/**
 * A fruit in the detail of its type: the list row without the type, which is the
 * enclosing one (Devil Fruit plan D12).
 *
 * @param image the image's path relative to the API root; {@code null} when none
 */
public record DevilFruitOfTypeResponse(UUID id, String slug, String romaji, String name, String image) {

}
