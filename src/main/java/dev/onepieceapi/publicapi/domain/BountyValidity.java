package dev.onepieceapi.publicapi.domain;

/**
 * Whether a bounty still holds, derived from the bounties online (Bounty plan D3):
 * {@code INACTIVE} when revoked, suspended or its holder dead; else {@code SUPERSEDED}
 * when an online bounty replaces it; else {@code ACTIVE}.
 */
public enum BountyValidity {

	ACTIVE, SUPERSEDED, INACTIVE

}
