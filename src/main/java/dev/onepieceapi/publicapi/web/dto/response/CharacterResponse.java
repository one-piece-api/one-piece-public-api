package dev.onepieceapi.publicapi.web.dto.response;

import dev.onepieceapi.publicapi.domain.CharacterStatus;
import dev.onepieceapi.publicapi.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.time.MonthDay;
import java.util.List;
import java.util.UUID;

/**
 * A Character in one language, as of today. Every field is always present, {@code null}
 * when missing, the lists empty; nothing of the editorial workflow shows.
 *
 * @param japaneseName as published
 * @param birthday ISO 8601 day and month, no year
 * @param description its paragraphs, in order (workflow 3.7.3)
 * @param image the main portrait's path relative to the API root (workflow 3.5.1);
 * {@code null} when none
 * @param epithets those written in the language of the path, in order
 * @param aliases the same in every language, in order
 * @param species its species online, in order
 * @param affiliations its memberships, in order
 * @param devilFruits its Devil Fruits, in order
 * @param relatives its relatives, whichever of the two characters stores the link: the
 * ones it stores in order, then the others by name
 * @param bounties its bounties online, highest amount first
 * @param firstAppearance where the reader first meets it
 * @param gallery its images, in order
 * @param initialKnowledge what the reader believes at the first appearance; {@code null}
 * when nothing is revealed later
 * @param reveals what the reader learns later, in order
 */
@Builder
public record CharacterResponse(UUID id, String slug, String romaji, String japaneseName, String language, String name,
		List<ParagraphResponse> description, CharacterStatus status, Gender gender,
		@Schema(type = "string", description = "Day and month, ISO 8601 without year", example = "--05-05",
				pattern = "^--\\d{2}-\\d{2}$") MonthDay birthday,
		String image, List<TextEntryResponse> epithets, List<TextEntryResponse> aliases,
		List<SpeciesEntryResponse> species, List<MembershipResponse> affiliations, List<FruitUseResponse> devilFruits,
		List<RelativeResponse> relatives, List<HeldBountyResponse> bounties, Instant publishedAt,
		FirstAppearanceResponse firstAppearance, List<GalleryImageResponse> gallery,
		CharacterKnowledgeResponse initialKnowledge, List<CharacterRevealResponse> reveals) {

}
