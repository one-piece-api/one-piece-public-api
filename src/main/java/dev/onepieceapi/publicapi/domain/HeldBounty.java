package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A bounty inside the character that holds it.
 *
 * @param slug the address derived from the poster name and the amount
 * @param amount in Berry
 */
public record HeldBounty(UUID id, String slug, long amount, BountyValidity validity) {

}
