package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.time.MonthDay;
import java.util.List;
import java.util.UUID;

/**
 * A Character online, in one language, as of today (Character plan D1-D8).
 *
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param japaneseName as published; null when not stated
 * @param birthday day and month; null when not stated
 * @param imageId the portrait's identifier; null when it has none
 * @param publishedAt when the online version was published
 * @param firstAppearance where the reader first meets it
 * @param epithets those written in its language, in the version's order
 * @param aliases the same in every language, in the version's order
 * @param species its species online, in the version's order
 * @param affiliations its memberships in affiliations online, in the version's order
 * @param bounties its bounties online, highest amount first
 */
@Builder
public record Character(UUID id, String slug, String romaji, String japaneseName, String language, String name,
		String description, CharacterStatus status, Gender gender, MonthDay birthday, String imageId,
		Instant publishedAt, FirstAppearance firstAppearance, @With List<String> epithets, @With List<String> aliases,
		@With List<SpeciesReference> species, @With List<Membership> affiliations,
		@With List<HeldBounty> bounties) implements PublishedContent {

	public Character {
		epithets = epithets == null ? List.of() : List.copyOf(epithets);
		aliases = aliases == null ? List.of() : List.copyOf(aliases);
		species = species == null ? List.of() : List.copyOf(species);
		affiliations = affiliations == null ? List.of() : List.copyOf(affiliations);
		bounties = bounties == null ? List.of() : List.copyOf(bounties);
	}

}
