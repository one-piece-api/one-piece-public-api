package dev.onepieceapi.publicapi.domain;

/**
 * A language the content is available in.
 *
 * @param code two lowercase letters
 * @param name the language's name in itself
 */
public record Language(String code, String name) {

}
