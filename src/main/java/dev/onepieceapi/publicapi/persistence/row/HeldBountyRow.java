package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/** A bounty of a character, read from {@code published.bounty} in its language. */
public record HeldBountyRow(UUID id, String slug, long amount, String validity) {

}
