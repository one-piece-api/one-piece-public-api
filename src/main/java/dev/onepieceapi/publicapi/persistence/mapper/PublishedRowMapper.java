package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.ChapterReference;
import dev.onepieceapi.publicapi.domain.DevilFruit;
import dev.onepieceapi.publicapi.domain.DevilFruitSubcategory;
import dev.onepieceapi.publicapi.domain.DevilFruitSubcategoryReference;
import dev.onepieceapi.publicapi.domain.DevilFruitSubcategorySummary;
import dev.onepieceapi.publicapi.domain.DevilFruitSummary;
import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeReference;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.FirstAppearance;
import dev.onepieceapi.publicapi.domain.Language;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitSubcategoryRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.LanguageRow;
import lombok.experimental.UtilityClass;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

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
			.firstAppearance(firstAppearanceOf(row.firstAppearanceChapterId(), row.firstAppearanceChapterSlug(),
					row.firstAppearanceChapterNumber(), row.firstAppearanceChapterTitle()))
			.build();
	}

	public DevilFruitTypeSummary toDomain(DevilFruitTypeSummaryRow row) {
		return DevilFruitTypeSummary.builder()
			.id(row.id())
			.slug(row.slug())
			.romaji(row.romaji())
			.name(row.name())
			.firstAppearance(firstAppearanceOf(row.firstAppearanceChapterId(), row.firstAppearanceChapterSlug(),
					row.firstAppearanceChapterNumber(), row.firstAppearanceChapterTitle()))
			.build();
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
			.type(typeOf(row.typeId(), row.typeSlug(), row.typeRomaji(), row.typeName()))
			.subcategory(subcategoryOf(row))
			.imageId(row.imageId())
			.publishedAt(row.publishedAt().toInstant().truncatedTo(ChronoUnit.SECONDS))
			.firstAppearance(firstAppearanceOf(row.firstAppearanceChapterId(), row.firstAppearanceChapterSlug(),
					row.firstAppearanceChapterNumber(), row.firstAppearanceChapterTitle()))
			.build();
	}

	public DevilFruitSummary toDomain(DevilFruitSummaryRow row) {
		return DevilFruitSummary.builder()
			.id(row.id())
			.slug(row.slug())
			.romaji(row.romaji())
			.name(row.name())
			.type(typeOf(row.typeId(), row.typeSlug(), row.typeRomaji(), row.typeName()))
			.subcategory(subcategoryOf(row))
			.imageId(row.imageId())
			.firstAppearance(firstAppearanceOf(row.firstAppearanceChapterId(), row.firstAppearanceChapterSlug(),
					row.firstAppearanceChapterNumber(), row.firstAppearanceChapterTitle()))
			.build();
	}

	public DevilFruitSubcategory toDomain(DevilFruitSubcategoryRow row) {
		return new DevilFruitSubcategory(row.id(), row.name(), row.description(),
				firstAppearanceOf(row.firstAppearanceChapterId(), row.firstAppearanceChapterSlug(),
						row.firstAppearanceChapterNumber(), row.firstAppearanceChapterTitle()));
	}

	/** The view leaves these null for a fruit with no subcategory (or no text for it). */
	/** None when the spoiler filter hides it (plan of the timeline, TL6). */
	private DevilFruitTypeReference typeOf(UUID id, String slug, String romaji, String name) {
		return id == null ? null : new DevilFruitTypeReference(id, slug, romaji, name);
	}

	private DevilFruitSubcategoryReference subcategoryOf(DevilFruitRow row) {
		return row.subcategoryId() == null ? null : new DevilFruitSubcategoryReference(row.subcategoryId(),
				row.subcategoryName(), row.subcategoryDescription());
	}

	private DevilFruitSubcategorySummary subcategoryOf(DevilFruitSummaryRow row) {
		return row.subcategoryId() == null ? null
				: new DevilFruitSubcategorySummary(row.subcategoryId(), row.subcategoryName());
	}

	/**
	 * The views leave these null for a content with no first appearance (an optional one,
	 * a subcategory's), or whose chapter has no text in the language.
	 */
	private FirstAppearance firstAppearanceOf(UUID id, String slug, Integer number, String title) {
		return id == null || number == null ? null : new FirstAppearance(new ChapterReference(id, slug, number, title));
	}

}
