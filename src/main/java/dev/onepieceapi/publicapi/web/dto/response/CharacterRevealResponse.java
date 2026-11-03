package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.CharacterField;
import dev.onepieceapi.publicapi.domain.CharacterStatus;

import java.util.List;

/**
 * A point where the reader learns some fields of a character (workflow 3.7.1). Each field
 * named carries its value, the current one included; the others are {@code null}.
 *
 * @param title in the language of the path
 * @param revealed where the reader learns
 * @param fields the fields learned, {@code IDENTITY} being the name, the romaji and the
 * Japanese name
 * @param name in the language of the path
 * @param slug the address of the romaji
 */
public record CharacterRevealResponse(String title, StoryPointResponse revealed, List<CharacterField> fields,
		String name, String romaji, String slug, String japaneseName, CharacterStatus status) {

}
