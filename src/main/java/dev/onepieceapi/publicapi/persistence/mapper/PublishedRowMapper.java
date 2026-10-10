package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.DevilFruit;
import dev.onepieceapi.publicapi.domain.DevilFruitSubcategory;
import dev.onepieceapi.publicapi.domain.DevilFruitSubcategorySummary;
import dev.onepieceapi.publicapi.domain.DevilFruitSummary;
import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.Language;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitSubcategoryRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitSummaryRow;
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

	public DevilFruit toDomain(DevilFruitRow row) {
		return DevilFruit.builder()
			.id(row.id())
			.slug(row.slug())
			.romaji(row.romaji())
			.language(row.language())
			.name(row.name())
			.description(row.description())
			.advantages(row.advantages())
			.disadvantages(row.disadvantages())
			.type(new DevilFruitTypeSummary(row.typeId(), row.typeSlug(), row.typeRomaji(), row.typeName()))
			.subcategory(subcategoryOf(row))
			.imageId(row.imageId())
			.publishedAt(row.publishedAt().toInstant().truncatedTo(ChronoUnit.SECONDS))
			.build();
	}

	public DevilFruitSummary toDomain(DevilFruitSummaryRow row) {
		return DevilFruitSummary.builder()
			.id(row.id())
			.slug(row.slug())
			.romaji(row.romaji())
			.name(row.name())
			.type(new DevilFruitTypeSummary(row.typeId(), row.typeSlug(), row.typeRomaji(), row.typeName()))
			.subcategory(subcategoryOf(row))
			.imageId(row.imageId())
			.build();
	}

	public DevilFruitSubcategory toDomain(DevilFruitSubcategoryRow row) {
		return new DevilFruitSubcategory(row.id(), row.name(), row.description());
	}

	/** The view leaves these null for a fruit with no subcategory (or no text for it). */
	private DevilFruitSubcategory subcategoryOf(DevilFruitRow row) {
		return row.subcategoryId() == null ? null
				: new DevilFruitSubcategory(row.subcategoryId(), row.subcategoryName(), row.subcategoryDescription());
	}

	private DevilFruitSubcategorySummary subcategoryOf(DevilFruitSummaryRow row) {
		return row.subcategoryId() == null ? null
				: new DevilFruitSubcategorySummary(row.subcategoryId(), row.subcategoryName());
	}

}
