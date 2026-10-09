package dev.onepieceapi.publicapi.domain;

/**
 * An image online: its bytes and their media type (plan D13).
 *
 * @param contentType the media type of the bytes
 */
public record Image(String contentType, byte[] bytes) {

}
