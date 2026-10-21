package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.BountyValidity;

import java.util.UUID;

/**
 * A bounty of a character, inside the character.
 *
 * @param slug the address derived from the poster name and the amount
 * @param amount in Berry
 * @param validity derived from the bounties online
 */
public record HeldBountyResponse(UUID id, String slug, long amount, BountyValidity validity) {

}
