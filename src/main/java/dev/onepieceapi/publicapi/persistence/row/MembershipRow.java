package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * A membership of a character, read from {@code published.character_affiliation} with the
 * affiliation and the role in the language of the path.
 */
public record MembershipRow(UUID id, String slug, String name, String kind, String role, String marineRank,
		boolean former) {

}
