package dev.onepieceapi.publicapi.web.mapper;

import dev.onepieceapi.publicapi.domain.Arc;
import dev.onepieceapi.publicapi.domain.ArcReference;
import dev.onepieceapi.publicapi.domain.ArcSummary;
import dev.onepieceapi.publicapi.domain.Chapter;
import dev.onepieceapi.publicapi.domain.ChapterReference;
import dev.onepieceapi.publicapi.domain.ChapterSummary;
import dev.onepieceapi.publicapi.domain.DevilFruit;
import dev.onepieceapi.publicapi.domain.DevilFruitSubcategory;
import dev.onepieceapi.publicapi.domain.DevilFruitSubcategoryReference;
import dev.onepieceapi.publicapi.domain.DevilFruitSubcategorySummary;
import dev.onepieceapi.publicapi.domain.DevilFruitSummary;
import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeReference;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.Episode;
import dev.onepieceapi.publicapi.domain.EpisodeSummary;
import dev.onepieceapi.publicapi.domain.FirstAppearance;
import dev.onepieceapi.publicapi.domain.Language;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.domain.Release;
import dev.onepieceapi.publicapi.domain.Saga;
import dev.onepieceapi.publicapi.domain.SagaSummary;
import dev.onepieceapi.publicapi.domain.StorySpan;
import dev.onepieceapi.publicapi.domain.Volume;
import dev.onepieceapi.publicapi.domain.VolumeSummary;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.dto.response.ArcReferenceResponse;
import dev.onepieceapi.publicapi.web.dto.response.ArcResponse;
import dev.onepieceapi.publicapi.web.dto.response.ArcSummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.ChapterReferenceResponse;
import dev.onepieceapi.publicapi.web.dto.response.ChapterResponse;
import dev.onepieceapi.publicapi.web.dto.response.ChapterSummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitOfTypeResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitSubcategoryReferenceResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitSubcategoryResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitSubcategorySummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitSummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitTypeReferenceResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitTypeResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitTypeSummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.EpisodeResponse;
import dev.onepieceapi.publicapi.web.dto.response.EpisodeSummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.FirstAppearanceResponse;
import dev.onepieceapi.publicapi.web.dto.response.LanguageResponse;
import dev.onepieceapi.publicapi.web.dto.response.PageResponse;
import dev.onepieceapi.publicapi.web.dto.response.ReleaseResponse;
import dev.onepieceapi.publicapi.web.dto.response.SagaResponse;
import dev.onepieceapi.publicapi.web.dto.response.SagaSummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.StorySpanResponse;
import dev.onepieceapi.publicapi.web.dto.response.VolumeResponse;
import dev.onepieceapi.publicapi.web.dto.response.VolumeSummaryResponse;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

/**
 * From the domain to the bodies of the public contract. The one place that decides what
 * the JSON says, so that a static generator could later produce the same bytes (plan
 * D14).
 */
@UtilityClass
public class PublicResponseMapper {

	public LanguageResponse toResponse(Language language) {
		return new LanguageResponse(language.code(), language.name());
	}

	public DevilFruitTypeResponse toResponse(DevilFruitType devilFruitType) {
		return DevilFruitTypeResponse.builder()
			.id(devilFruitType.id())
			.slug(devilFruitType.slug())
			.romaji(devilFruitType.romaji())
			.language(devilFruitType.language())
			.name(devilFruitType.name())
			.description(devilFruitType.description())
			.advantages(devilFruitType.advantages())
			.disadvantages(devilFruitType.disadvantages())
			.publishedAt(devilFruitType.publishedAt())
			.subcategories(devilFruitType.subcategories().stream().map(PublicResponseMapper::toResponse).toList())
			.devilFruits(devilFruitType.devilFruits().stream().map(PublicResponseMapper::toTypeFruitResponse).toList())
			.firstAppearance(toResponse(devilFruitType.firstAppearance()))
			.build();
	}

	public DevilFruitTypeSummaryResponse toResponse(DevilFruitTypeSummary summary) {
		return DevilFruitTypeSummaryResponse.builder()
			.id(summary.id())
			.slug(summary.slug())
			.romaji(summary.romaji())
			.name(summary.name())
			.firstAppearance(toResponse(summary.firstAppearance()))
			.build();
	}

	public DevilFruitTypeReferenceResponse toResponse(DevilFruitTypeReference type) {
		return new DevilFruitTypeReferenceResponse(type.id(), type.slug(), type.romaji(), type.name());
	}

	/** {@code null} when the content has none (a subcategory's is optional). */
	public FirstAppearanceResponse toResponse(FirstAppearance firstAppearance) {
		return firstAppearance == null ? null : new FirstAppearanceResponse(toResponse(firstAppearance.chapter()));
	}

	public ChapterReferenceResponse toResponse(ChapterReference chapter) {
		return new ChapterReferenceResponse(chapter.id(), chapter.slug(), chapter.number(), chapter.title());
	}

	public DevilFruitResponse toResponse(DevilFruit devilFruit) {
		return DevilFruitResponse.builder()
			.id(devilFruit.id())
			.slug(devilFruit.slug())
			.romaji(devilFruit.romaji())
			.language(devilFruit.language())
			.name(devilFruit.name())
			.description(devilFruit.description())
			.advantages(devilFruit.advantages())
			.disadvantages(devilFruit.disadvantages())
			.type(toResponse(devilFruit.type()))
			.subcategory(toResponse(devilFruit.subcategory()))
			.image(imagePath(devilFruit.imageId()))
			.publishedAt(devilFruit.publishedAt())
			.firstAppearance(toResponse(devilFruit.firstAppearance()))
			.build();
	}

	public DevilFruitSubcategoryResponse toResponse(DevilFruitSubcategory subcategory) {
		return new DevilFruitSubcategoryResponse(subcategory.id(), subcategory.name(), subcategory.description(),
				toResponse(subcategory.firstAppearance()));
	}

	/** {@code null} when the fruit names none. */
	public DevilFruitSubcategoryReferenceResponse toResponse(DevilFruitSubcategoryReference subcategory) {
		return subcategory == null ? null : new DevilFruitSubcategoryReferenceResponse(subcategory.id(),
				subcategory.name(), subcategory.description());
	}

	/** {@code null} when the fruit names none. */
	public DevilFruitSubcategorySummaryResponse toResponse(DevilFruitSubcategorySummary subcategory) {
		return subcategory == null ? null
				: new DevilFruitSubcategorySummaryResponse(subcategory.id(), subcategory.name());
	}

	public DevilFruitSummaryResponse toResponse(DevilFruitSummary summary) {
		return DevilFruitSummaryResponse.builder()
			.id(summary.id())
			.slug(summary.slug())
			.romaji(summary.romaji())
			.name(summary.name())
			.type(toResponse(summary.type()))
			.subcategory(toResponse(summary.subcategory()))
			.image(imagePath(summary.imageId()))
			.firstAppearance(toResponse(summary.firstAppearance()))
			.build();
	}

	/** A fruit inside its type: the type is the enclosing one, so it is left out. */
	public DevilFruitOfTypeResponse toTypeFruitResponse(DevilFruitSummary summary) {
		return new DevilFruitOfTypeResponse(summary.id(), summary.slug(), summary.romaji(), summary.name(),
				imagePath(summary.imageId()));
	}

	public SagaResponse toResponse(Saga saga) {
		return SagaResponse.builder()
			.id(saga.id())
			.slug(saga.slug())
			.number(saga.number())
			.language(saga.language())
			.title(saga.title())
			.description(saga.description())
			.chapters(toResponse(saga.chapters()))
			.episodes(toResponse(saga.episodes()))
			.arcs(saga.arcs().stream().map(PublicResponseMapper::toArcReferenceResponse).toList())
			.publishedAt(saga.publishedAt())
			.build();
	}

	public SagaSummaryResponse toResponse(SagaSummary summary) {
		return new SagaSummaryResponse(summary.id(), summary.slug(), summary.number(), summary.title());
	}

	public ArcResponse toResponse(Arc arc) {
		return ArcResponse.builder()
			.id(arc.id())
			.slug(arc.slug())
			.number(arc.number())
			.language(arc.language())
			.title(arc.title())
			.description(arc.description())
			.saga(toResponse(arc.saga()))
			.chapters(toResponse(arc.chapters()))
			.episodes(toResponse(arc.episodes()))
			.publishedAt(arc.publishedAt())
			.build();
	}

	public ArcSummaryResponse toResponse(ArcSummary summary) {
		return new ArcSummaryResponse(summary.id(), summary.slug(), summary.number(), summary.title(),
				toResponse(summary.saga()));
	}

	/** An arc inside the content that relates to it: its saga is left out. */
	public ArcReferenceResponse toArcReferenceResponse(ArcSummary summary) {
		return new ArcReferenceResponse(summary.id(), summary.slug(), summary.number(), summary.title());
	}

	/** {@code null} when the content names none (a filler episode). */
	public ArcReferenceResponse toResponse(ArcReference arc) {
		return arc == null ? null : new ArcReferenceResponse(arc.id(), arc.slug(), arc.number(), arc.title());
	}

	public VolumeResponse toResponse(Volume volume) {
		return VolumeResponse.builder()
			.id(volume.id())
			.slug(volume.slug())
			.number(volume.number())
			.language(volume.language())
			.title(volume.title())
			.japaneseTitle(volume.japaneseTitle())
			.cover(imagePath(volume.coverImageId()))
			.chapters(toResponse(volume.chapters()))
			.arcs(volume.arcs().stream().map(PublicResponseMapper::toArcReferenceResponse).toList())
			.releases(volume.releases().stream().map(PublicResponseMapper::toResponse).toList())
			.publishedAt(volume.publishedAt())
			.build();
	}

	/** {@code null} when the content names none (a chapter not collected in a volume). */
	public VolumeSummaryResponse toResponse(VolumeSummary summary) {
		return summary == null ? null : new VolumeSummaryResponse(summary.id(), summary.slug(), summary.number(),
				summary.title(), imagePath(summary.coverImageId()));
	}

	public ChapterResponse toResponse(Chapter chapter) {
		return ChapterResponse.builder()
			.id(chapter.id())
			.slug(chapter.slug())
			.number(chapter.number())
			.language(chapter.language())
			.title(chapter.title())
			.description(chapter.description())
			.japaneseTitle(chapter.japaneseTitle())
			.pageCount(chapter.pageCount())
			.arc(toResponse(chapter.arc()))
			.volume(toResponse(chapter.volume()))
			.releases(chapter.releases().stream().map(PublicResponseMapper::toResponse).toList())
			.publishedAt(chapter.publishedAt())
			.build();
	}

	public ChapterSummaryResponse toResponse(ChapterSummary summary) {
		return new ChapterSummaryResponse(summary.id(), summary.slug(), summary.number(), summary.title(),
				toResponse(summary.arc()), toResponse(summary.volume()));
	}

	public EpisodeResponse toResponse(Episode episode) {
		return EpisodeResponse.builder()
			.id(episode.id())
			.slug(episode.slug())
			.number(episode.number())
			.language(episode.language())
			.title(episode.title())
			.description(episode.description())
			.japaneseTitle(episode.japaneseTitle())
			.arc(toResponse(episode.arc()))
			.broadcasts(episode.broadcasts().stream().map(PublicResponseMapper::toResponse).toList())
			.publishedAt(episode.publishedAt())
			.build();
	}

	public EpisodeSummaryResponse toResponse(EpisodeSummary summary) {
		return new EpisodeSummaryResponse(summary.id(), summary.slug(), summary.number(), summary.title(),
				toResponse(summary.arc()));
	}

	public StorySpanResponse toResponse(StorySpan span) {
		return new StorySpanResponse(span.count(), span.firstNumber(), span.lastNumber(), span.firstDate(),
				span.lastDate());
	}

	public ReleaseResponse toResponse(Release release) {
		return new ReleaseResponse(release.country(), release.date());
	}

	public <T, R> PageResponse<R> toResponse(Page<T> page, Function<T, R> itemMapper) {
		return new PageResponse<>(page.content().stream().map(itemMapper).toList(), page.page(), page.size(),
				page.totalElements(), page.totalPages());
	}

	/**
	 * The address of an image relative to the API root, as every path the API gives (plan
	 * D7); {@code null} when there is none.
	 */
	private String imagePath(String imageId) {
		return imageId == null ? null : ApiPaths.IMAGE.substring(1).replace("{id}", imageId);
	}

}
