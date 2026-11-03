package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * A Devil Fruit online, in one language.
 *
 * @param id the content's identifier
 * @param slug the address derived from the romaji; null when the romaji leaves none
 * @param type its type, in the same language
 * @param subcategory the subcategory of its type it names; null when none
 * @param imageId the image's identifier; null when the fruit has none
 * @param publishedAt when the online version was published
 * @param firstAppearance where the reader first meets it
 * @param multipleUsers whether several characters may have its power at once (SMILE)
 * @param users the characters online that have eaten it, those who have its power now
 * first
 * @param reveals what the reader believes at the first appearance and learns later
 */
@Builder(toBuilder = true)
public record DevilFruit(UUID id, String slug, String romaji, String language, String name, List<Paragraph> description,
		List<Paragraph> advantages, List<Paragraph> disadvantages, DevilFruitTypeReference type,
		DevilFruitSubcategoryReference subcategory, String imageId, Instant publishedAt,
		FirstAppearance firstAppearance, boolean multipleUsers, @With List<FruitUser> users,
		FieldReveals<DevilFruitField, DevilFruitValues> reveals) implements PublishedContent, SeenByReader<DevilFruit> {

	public DevilFruit {
		description = description == null ? List.of() : List.copyOf(description);
		advantages = advantages == null ? List.of() : List.copyOf(advantages);
		disadvantages = disadvantages == null ? List.of() : List.copyOf(disadvantages);
		users = users == null ? List.of() : List.copyOf(users);
		reveals = reveals == null ? FieldReveals.none() : reveals;
	}

	/** Its long texts as paragraphs (workflow 3.7.3). */
	public DevilFruit withParagraphs(LongTexts texts) {
		return toBuilder().description(texts.description())
			.advantages(texts.advantages())
			.disadvantages(texts.disadvantages())
			.build();
	}

	/**
	 * Its reveals, each reveal of the current value carrying that value (reveals plan R7
	 * S7).
	 */
	public DevilFruit withReveals(FieldReveals<DevilFruitField, DevilFruitValues> stored) {
		return toBuilder().reveals(stored.filledWith(currentValues())).build();
	}

	/** What the main fields say: the current value of every revealable field. */
	public DevilFruitValues currentValues() {
		return new DevilFruitValues(this.name, this.romaji, this.slug, this.type, this.subcategory);
	}

	/**
	 * As the reader sees it at N: name, romaji, type and subcategory as known then
	 * (workflow 3.7.1), a type not met yet left out with its subcategory; the reveals
	 * reached and the users met, named as known then.
	 */
	@Override
	public DevilFruit seenBy(ReaderSight sight) {
		DevilFruitValues known = sight.devilFruitAt(this.id, currentValues());
		DevilFruitTypeReference typeSeen = sight.ifSeen(known.type(), DevilFruitTypeReference::id);
		DevilFruitSubcategoryReference subcategorySeen = (typeSeen != null)
				? sight.ifSeen(known.subcategory(), DevilFruitSubcategoryReference::id) : null;
		return toBuilder().name(known.name())
			.romaji(known.romaji())
			.slug(known.slug())
			.type(typeSeen)
			.subcategory(subcategorySeen)
			.reveals(sight.reached(this.reveals))
			.firstAppearance(sight.onAxis(this.firstAppearance))
			.users(usersSeenBy(sight))
			.build();
	}

	/**
	 * The users the reader knows of, those who have its power at N first, then by name
	 * (reveals plan R7 S13).
	 */
	private List<FruitUser> usersSeenBy(ReaderSight sight) {
		Comparator<FruitUser> order = Comparator.comparing(FruitUser::former)
			.thenComparing(FruitUser::name, NameOrder.in(this.language))
			.thenComparing(FruitUser::characterId);
		return sight.discovered(this.users, FruitUser::characterId, FruitUser::withWindow)
			.stream()
			.map((user) -> user.namedBy(sight))
			.sorted(order)
			.toList();
	}

}
