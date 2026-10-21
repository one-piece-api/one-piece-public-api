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
 * @param image the portrait's path relative to the API root; {@code null} when none
 * @param epithets those written in the language of the path, in order
 * @param aliases the same in every language, in order
 * @param species its species online, in order
 * @param firstAppearance where the reader first meets it
 */
@Builder
public record CharacterResponse(UUID id, String slug, String romaji, String japaneseName, String language, String name,
		String description, CharacterStatus status, Gender gender,
		@Schema(type = "string", description = "Day and month, ISO 8601 without year", example = "--05-05",
				pattern = "^--\\d{2}-\\d{2}$") MonthDay birthday,
		String image, List<String> epithets, List<String> aliases, List<SpeciesReferenceResponse> species,
		Instant publishedAt, FirstAppearanceResponse firstAppearance) {

}
