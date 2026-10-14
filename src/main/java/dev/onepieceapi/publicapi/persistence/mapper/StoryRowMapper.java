package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.Arc;
import dev.onepieceapi.publicapi.domain.ArcReference;
import dev.onepieceapi.publicapi.domain.ArcSummary;
import dev.onepieceapi.publicapi.domain.Chapter;
import dev.onepieceapi.publicapi.domain.ChapterSummary;
import dev.onepieceapi.publicapi.domain.Episode;
import dev.onepieceapi.publicapi.domain.EpisodeSummary;
import dev.onepieceapi.publicapi.domain.Release;
import dev.onepieceapi.publicapi.domain.Saga;
import dev.onepieceapi.publicapi.domain.SagaSummary;
import dev.onepieceapi.publicapi.domain.StorySpan;
import dev.onepieceapi.publicapi.domain.Volume;
import dev.onepieceapi.publicapi.domain.VolumeSummary;
import dev.onepieceapi.publicapi.persistence.row.ArcRow;
import dev.onepieceapi.publicapi.persistence.row.ArcSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.ChapterRow;
import dev.onepieceapi.publicapi.persistence.row.ChapterSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.EpisodeRow;
import dev.onepieceapi.publicapi.persistence.row.EpisodeSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.ReleaseRow;
import dev.onepieceapi.publicapi.persistence.row.SagaRow;
import dev.onepieceapi.publicapi.persistence.row.SagaSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.VolumeRow;
import dev.onepieceapi.publicapi.persistence.row.VolumeSummaryRow;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * From the rows of the story views of the {@code published} schema to the domain. The
 * title is the view's {@code name} column: that is what the generic repository searches
 * and sorts.
 */
@UtilityClass
public class StoryRowMapper {

	public Saga toDomain(SagaRow row) {
		return Saga.builder()
			.id(row.id())
			.slug(row.slug())
			.number(row.number())
			.language(row.language())
			.title(row.name())
			.description(row.description())
			.chapters(new StorySpan(row.chapterCount(), row.firstChapterNumber(), row.lastChapterNumber(),
					row.firstChapterDate(), row.lastChapterDate()))
			.episodes(new StorySpan(row.episodeCount(), row.firstEpisodeNumber(), row.lastEpisodeNumber(),
					row.firstEpisodeDate(), row.lastEpisodeDate()))
			.publishedAt(publishedAt(row.publishedAt()))
			.build();
	}

	public SagaSummary toDomain(SagaSummaryRow row) {
		return new SagaSummary(row.id(), row.slug(), row.number(), row.name());
	}

	public Arc toDomain(ArcRow row) {
		return Arc.builder()
			.id(row.id())
			.slug(row.slug())
			.number(row.number())
			.language(row.language())
			.title(row.name())
			.description(row.description())
			.saga(new SagaSummary(row.sagaId(), row.sagaSlug(), row.sagaNumber(), row.sagaTitle()))
			.chapters(new StorySpan(row.chapterCount(), row.firstChapterNumber(), row.lastChapterNumber(),
					row.firstChapterDate(), row.lastChapterDate()))
			.episodes(new StorySpan(row.episodeCount(), row.firstEpisodeNumber(), row.lastEpisodeNumber(),
					row.firstEpisodeDate(), row.lastEpisodeDate()))
			.publishedAt(publishedAt(row.publishedAt()))
			.build();
	}

	public ArcSummary toDomain(ArcSummaryRow row) {
		return new ArcSummary(row.id(), row.slug(), row.number(), row.name(),
				new SagaSummary(row.sagaId(), row.sagaSlug(), row.sagaNumber(), row.sagaTitle()));
	}

	public Volume toDomain(VolumeRow row) {
		return Volume.builder()
			.id(row.id())
			.slug(row.slug())
			.number(row.number())
			.language(row.language())
			.title(row.name())
			.japaneseTitle(row.japaneseTitle())
			.coverImageId(row.coverImageId())
			.chapters(new StorySpan(row.chapterCount(), row.firstChapterNumber(), row.lastChapterNumber(),
					row.firstChapterDate(), row.lastChapterDate()))
			.publishedAt(publishedAt(row.publishedAt()))
			.build();
	}

	public VolumeSummary toDomain(VolumeSummaryRow row) {
		return new VolumeSummary(row.id(), row.slug(), row.number(), row.name(), row.coverImageId());
	}

	public Chapter toDomain(ChapterRow row) {
		return Chapter.builder()
			.id(row.id())
			.slug(row.slug())
			.number(row.number())
			.language(row.language())
			.title(row.name())
			.description(row.description())
			.japaneseTitle(row.japaneseTitle())
			.pageCount(row.pageCount())
			.arc(new ArcReference(row.arcId(), row.arcSlug(), row.arcNumber(), row.arcTitle()))
			.volume(volumeOf(row.volumeId(), row.volumeSlug(), row.volumeNumber(), row.volumeTitle(),
					row.volumeCoverImageId()))
			.publishedAt(publishedAt(row.publishedAt()))
			.build();
	}

	public ChapterSummary toDomain(ChapterSummaryRow row) {
		return new ChapterSummary(row.id(), row.slug(), row.number(), row.name(),
				new ArcReference(row.arcId(), row.arcSlug(), row.arcNumber(), row.arcTitle()), volumeOf(row.volumeId(),
						row.volumeSlug(), row.volumeNumber(), row.volumeTitle(), row.volumeCoverImageId()));
	}

	public Episode toDomain(EpisodeRow row) {
		return Episode.builder()
			.id(row.id())
			.slug(row.slug())
			.number(row.number())
			.language(row.language())
			.title(row.name())
			.description(row.description())
			.japaneseTitle(row.japaneseTitle())
			.arc(arcOf(row.arcId(), row.arcSlug(), row.arcNumber(), row.arcTitle()))
			.publishedAt(publishedAt(row.publishedAt()))
			.build();
	}

	public EpisodeSummary toDomain(EpisodeSummaryRow row) {
		return new EpisodeSummary(row.id(), row.slug(), row.number(), row.name(),
				arcOf(row.arcId(), row.arcSlug(), row.arcNumber(), row.arcTitle()));
	}

	public Release toDomain(ReleaseRow row) {
		return new Release(row.country(), row.date());
	}

	/** The view leaves these null for a chapter with no volume (or no text for it). */
	private VolumeSummary volumeOf(UUID id, String slug, Integer number, String title, String coverImageId) {
		return id == null || number == null ? null : new VolumeSummary(id, slug, number, title, coverImageId);
	}

	/** The view leaves these null for an episode with no arc (a filler). */
	private ArcReference arcOf(UUID id, String slug, Integer number, String title) {
		return id == null || number == null ? null : new ArcReference(id, slug, number, title);
	}

	private Instant publishedAt(OffsetDateTime publishedAt) {
		return publishedAt.toInstant().truncatedTo(ChronoUnit.SECONDS);
	}

}
