package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A character inside the character that names it as a relative, or that is named by it.
 * The gender gives the label of the kind (Character plan D13).
 */
public record RelativeReference(UUID id, String slug, String name, Gender gender) {

}
