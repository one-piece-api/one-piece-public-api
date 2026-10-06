package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.Language;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.LanguageRow;
import lombok.experimental.UtilityClass;

import java.time.temporal.ChronoUnit;

/** From the rows of the {@code published} views to the domain. */
@UtilityClass
public class PublishedRowMapper {

	public Language toDomain(LanguageRow row) {
		return new Language(row.code(), row.name());
	}

	public DevilFruitType toDomain(DevilFruitTypeRow row) {
		return DevilFruitType.builder()
			.id(row.id())
			.slug(row.slug())
			.romaji(row.romaji())
			.language(row.language())
			.name(row.name())
			.description(row.description())
			.advantages(row.advantages())
			.disadvantages(row.disadvantages())
			.publishedAt(row.publishedAt().toInstant().truncatedTo(ChronoUnit.SECONDS))
			.build();
	}

	public DevilFruitTypeSummary toDomain(DevilFruitTypeSummaryRow row) {
		return new DevilFruitTypeSummary(row.id(), row.slug(), row.romaji(), row.name());
	}

}
