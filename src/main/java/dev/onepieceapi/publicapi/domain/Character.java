package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.Gallery;
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
 * @param imageId the main portrait's identifier, derived from the gallery (workflow
 * 3.5.1); null when it has none
 * @param publishedAt when the online version was published
 * @param firstAppearance where the reader first meets it
 * @param epithets those written in its language, in the version's order
 * @param aliases the same in every language, in the version's order
 * @param species its species online, in the version's order
 * @param affiliations its memberships in affiliations online, in the version's order
 * @param devilFruits its Devil Fruits online, in the version's order
 * @param relatives the ones it stores, in the version's order, then the characters online
 * that name it, by name, each through the inverse kind (Character plan D13)
 * @param bounties its bounties online, highest amount first
 * @param gallery its images, in the version's order
 * @param reveals what the reader believes at the first appearance and learns later
 */
@Builder(toBuilder = true)
public record Character(UUID id, String slug, String romaji, String japaneseName, String language, String name,
		List<Paragraph> description, CharacterStatus status, Gender gender, MonthDay birthday, String imageId,
		Instant publishedAt, FirstAppearance firstAppearance, @With List<TextEntry> epithets,
		@With List<TextEntry> aliases, @With List<SpeciesEntry> species, @With List<Membership> affiliations,
		@With List<FruitUse> devilFruits, @With List<Relative> relatives, @With List<HeldBounty> bounties,
		List<CharacterImage> gallery,
		FieldReveals<CharacterField, CharacterValues> reveals) implements PublishedContent, SeenByReader<Character> {

	public Character {
		description = description == null ? List.of() : List.copyOf(description);
		epithets = epithets == null ? List.of() : List.copyOf(epithets);
		aliases = aliases == null ? List.of() : List.copyOf(aliases);
		species = species == null ? List.of() : List.copyOf(species);
		affiliations = affiliations == null ? List.of() : List.copyOf(affiliations);
		devilFruits = devilFruits == null ? List.of() : List.copyOf(devilFruits);
		relatives = relatives == null ? List.of() : List.copyOf(relatives);
		bounties = bounties == null ? List.of() : List.copyOf(bounties);
		gallery = gallery == null ? List.of() : List.copyOf(gallery);
		reveals = reveals == null ? FieldReveals.none() : reveals;
	}

	/** Its description as paragraphs (workflow 3.7.3). */
	public Character withParagraphs(LongTexts texts) {
		return toBuilder().description(texts.description()).build();
	}

	/**
	 * Its gallery, the main portrait derived from it without a filter (workflow 3.5.1).
	 */
	public Character withGallery(List<CharacterImage> images) {
		String portrait = Gallery.mainPortrait(images).map(CharacterImage::imageId).orElse(null);
		return toBuilder().gallery(images).imageId(portrait).build();
	}

	/**
	 * Its reveals, each reveal of the current value carrying that value (reveals plan R7
	 * S7).
	 */
	public Character withReveals(FieldReveals<CharacterField, CharacterValues> stored) {
		return toBuilder().reveals(stored.filledWith(currentValues())).build();
	}

	/** What the main fields say: the current value of every revealable field. */
	public CharacterValues currentValues() {
		return new CharacterValues(this.name, this.romaji, this.slug, this.japaneseName, this.status);
	}

	@Override
	public Character seenBy(ReaderSight sight) {
		return toBuilder().firstAppearance(sight.onAxis(this.firstAppearance))
			.epithets(sight.discovered(this.epithets, TextEntry::withWindow))
			.aliases(sight.discovered(this.aliases, TextEntry::withWindow))
			.species(sight.discovered(this.species, SpeciesEntry::speciesId, SpeciesEntry::withWindow))
			.affiliations(membershipsSeenBy(sight))
			.devilFruits(sight.discovered(this.devilFruits, FruitUse::devilFruitId, FruitUse::withWindow))
			.relatives(sight.discovered(this.relatives, Relative::characterId, Relative::withWindow))
			.bounties(sight.seen(this.bounties, HeldBounty::id))
			.build();
	}

	/** The memberships in affiliations met with at least one period discovered. */
	private List<Membership> membershipsSeenBy(ReaderSight sight) {
		return sight.seen(this.affiliations, Membership::affiliationId)
			.stream()
			.map((membership) -> membership.seenBy(sight))
			.filter(Membership::known)
			.toList();
	}

}
