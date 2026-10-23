package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.Gender;

import java.util.UUID;

/** A character, inside the character that has it as a relative. */
public record RelativeReferenceResponse(UUID id, String slug, String name, Gender gender) {

}
