package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A Character inside the content that names it, such as the holder of a bounty. The name
 * is null when the character is not written in the language.
 */
public record CharacterReference(UUID id, String slug, String name) {

}
