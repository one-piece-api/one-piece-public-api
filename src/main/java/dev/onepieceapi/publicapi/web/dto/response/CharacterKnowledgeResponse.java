package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.CharacterStatus;

/**
 * What the reader believes of a character at its first appearance (workflow 3.7.1): only
 * the fields some reveal names, the others {@code null}.
 *
 * @param name in the language of the path
 * @param slug the address of the romaji
 */
public record CharacterKnowledgeResponse(String name, String romaji, String slug, String japaneseName,
		CharacterStatus status) {

}
