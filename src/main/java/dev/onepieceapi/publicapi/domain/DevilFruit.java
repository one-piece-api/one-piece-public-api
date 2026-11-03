package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
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
 */
@Builder(toBuilder = true)
public record DevilFruit(UUID id, String slug, String romaji, String language, String name, String description,
		String advantages, String disadvantages, DevilFruitTypeReference type,
		DevilFruitSubcategoryReference subcategory, String imageId, Instant publishedAt,
		FirstAppearance firstAppearance, boolean multipleUsers,
		@With List<FruitUser> users) implements PublishedContent, SeenByReader<DevilFruit> {

	public DevilFruit {
		users = users == null ? List.of() : List.copyOf(users);
	}

	@Override
	public DevilFruit seenBy(ReaderSight sight) {
		DevilFruitTypeReference typeSeen = sight.ifSeen(this.type, DevilFruitTypeReference::id);
		return toBuilder().type(typeSeen)
			.subcategory((typeSeen != null) ? sight.ifSeen(this.subcategory, DevilFruitSubcategoryReference::id) : null)
			.firstAppearance(sight.onAxis(this.firstAppearance))
			.users(sight.seen(this.users, FruitUser::characterId))
			.build();
	}

}
