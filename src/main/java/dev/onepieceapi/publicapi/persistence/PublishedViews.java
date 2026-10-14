package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.Arc;
import dev.onepieceapi.publicapi.domain.ArcSummary;
import dev.onepieceapi.publicapi.domain.Chapter;
import dev.onepieceapi.publicapi.domain.ChapterSummary;
import dev.onepieceapi.publicapi.domain.DevilFruit;
import dev.onepieceapi.publicapi.domain.DevilFruitSummary;
import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.Episode;
import dev.onepieceapi.publicapi.domain.EpisodeSummary;
import dev.onepieceapi.publicapi.domain.Saga;
import dev.onepieceapi.publicapi.domain.SagaSummary;
import dev.onepieceapi.publicapi.domain.Volume;
import dev.onepieceapi.publicapi.domain.VolumeSummary;
import dev.onepieceapi.publicapi.persistence.mapper.PublishedRowMapper;
import dev.onepieceapi.publicapi.persistence.mapper.StoryRowMapper;
import dev.onepieceapi.publicapi.persistence.row.ArcRow;
import dev.onepieceapi.publicapi.persistence.row.ArcSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.ChapterRow;
import dev.onepieceapi.publicapi.persistence.row.ChapterSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitTypeSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.EpisodeRow;
import dev.onepieceapi.publicapi.persistence.row.EpisodeSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.SagaRow;
import dev.onepieceapi.publicapi.persistence.row.SagaSummaryRow;
import dev.onepieceapi.publicapi.persistence.row.VolumeRow;
import dev.onepieceapi.publicapi.persistence.row.VolumeSummaryRow;

import java.util.Map;

/**
 * The entities online, one descriptor each. An explicit private constructor, as in
 * {@code ApiPaths}: checkstyle does not see the one Lombok would add.
 */
public final class PublishedViews {

	/** The name of the filter of the fruits by their type (plan D11). */
	public static final String TYPE_FILTER = "type";

	public static final PublishedView<DevilFruitType, DevilFruitTypeSummary> DEVIL_FRUIT_TYPE = PublishedView
		.<DevilFruitType, DevilFruitTypeSummary>builder()
		.entityType("DEVIL_FRUIT_TYPE")
		.view("published.devil_fruit_type")
		.detailColumns("id, slug, romaji, language, name, description, advantages, disadvantages, published_at")
		.summaryColumns("id, slug, romaji, name")
		.naturalKey("romaji")
		.detail(PublishedView.reading(DevilFruitTypeRow.class, PublishedRowMapper::toDomain))
		.summary(PublishedView.reading(DevilFruitTypeSummaryRow.class, PublishedRowMapper::toDomain))
		.build();

	public static final PublishedView<DevilFruit, DevilFruitSummary> DEVIL_FRUIT = PublishedView
		.<DevilFruit, DevilFruitSummary>builder()
		.entityType("DEVIL_FRUIT")
		.view("published.devil_fruit")
		.detailColumns("id, slug, romaji, language, name, description, advantages, disadvantages,"
				+ " type_id, type_slug, type_romaji, type_name, image_id, published_at,"
				+ " subcategory_id, subcategory_name, subcategory_description")
		.summaryColumns("id, slug, romaji, name, type_id, type_slug, type_romaji, type_name, image_id,"
				+ " subcategory_id, subcategory_name")
		.naturalKey("romaji")
		.detail(PublishedView.reading(DevilFruitRow.class, PublishedRowMapper::toDomain))
		.summary(PublishedView.reading(DevilFruitSummaryRow.class, PublishedRowMapper::toDomain))
		.filters(Map.of(TYPE_FILTER, ContentFilter.onContent("type_id", "DEVIL_FRUIT_TYPE")))
		.build();

	/** The names of the filters of the story entities by what they belong to. */
	public static final String SAGA_FILTER = "saga";

	public static final String ARC_FILTER = "arc";

	public static final String VOLUME_FILTER = "volume";

	public static final PublishedView<Saga, SagaSummary> SAGA = PublishedView.<Saga, SagaSummary>builder()
		.entityType("SAGA")
		.view("published.saga")
		.detailColumns("id, slug, number, language, name, description, published_at, chapter_count,"
				+ " first_chapter_number, last_chapter_number, first_chapter_date, last_chapter_date, episode_count,"
				+ " first_episode_number, last_episode_number, first_episode_date, last_episode_date")
		.summaryColumns("id, slug, number, name")
		.naturalKey("number")
		.detail(PublishedView.reading(SagaRow.class, StoryRowMapper::toDomain))
		.summary(PublishedView.reading(SagaSummaryRow.class, StoryRowMapper::toDomain))
		.build();

	public static final PublishedView<Arc, ArcSummary> ARC = PublishedView.<Arc, ArcSummary>builder()
		.entityType("ARC")
		.view("published.arc")
		.detailColumns("id, slug, number, language, name, description, saga_id, saga_slug, saga_number, saga_title,"
				+ " published_at, chapter_count, first_chapter_number, last_chapter_number, first_chapter_date,"
				+ " last_chapter_date, episode_count, first_episode_number, last_episode_number, first_episode_date,"
				+ " last_episode_date")
		.summaryColumns("id, slug, number, name, saga_id, saga_slug, saga_number, saga_title")
		.naturalKey("number")
		.detail(PublishedView.reading(ArcRow.class, StoryRowMapper::toDomain))
		.summary(PublishedView.reading(ArcSummaryRow.class, StoryRowMapper::toDomain))
		.filters(Map.of(SAGA_FILTER, ContentFilter.onContent("saga_id", "SAGA"), VOLUME_FILTER, arcsOfVolume()))
		.embeddedOrder("number")
		.build();

	public static final PublishedView<Volume, VolumeSummary> VOLUME = PublishedView.<Volume, VolumeSummary>builder()
		.entityType("VOLUME")
		.view("published.volume")
		.detailColumns("id, slug, number, language, name, japanese_title, cover_image_id, published_at, chapter_count,"
				+ " first_chapter_number, last_chapter_number, first_chapter_date, last_chapter_date")
		.summaryColumns("id, slug, number, name, cover_image_id")
		.naturalKey("number")
		.detail(PublishedView.reading(VolumeRow.class, StoryRowMapper::toDomain))
		.summary(PublishedView.reading(VolumeSummaryRow.class, StoryRowMapper::toDomain))
		.build();

	public static final PublishedView<Chapter, ChapterSummary> CHAPTER = PublishedView
		.<Chapter, ChapterSummary>builder()
		.entityType("CHAPTER")
		.view("published.chapter")
		.detailColumns("id, slug, number, language, name, description, japanese_title, page_count, arc_id, arc_slug,"
				+ " arc_number, arc_title, volume_id, volume_slug, volume_number, volume_title,"
				+ " volume_cover_image_id, published_at")
		.summaryColumns("id, slug, number, name, arc_id, arc_slug, arc_number, arc_title, volume_id, volume_slug,"
				+ " volume_number, volume_title, volume_cover_image_id")
		.naturalKey("number")
		.detail(PublishedView.reading(ChapterRow.class, StoryRowMapper::toDomain))
		.summary(PublishedView.reading(ChapterSummaryRow.class, StoryRowMapper::toDomain))
		.filters(Map.of(ARC_FILTER, ContentFilter.onContent("arc_id", "ARC"), VOLUME_FILTER,
				ContentFilter.onContent("volume_id", "VOLUME")))
		.build();

	public static final PublishedView<Episode, EpisodeSummary> EPISODE = PublishedView
		.<Episode, EpisodeSummary>builder()
		.entityType("EPISODE")
		.view("published.episode")
		.detailColumns("id, slug, number, language, name, description, japanese_title, arc_id, arc_slug, arc_number,"
				+ " arc_title, published_at")
		.summaryColumns("id, slug, number, name, arc_id, arc_slug, arc_number, arc_title")
		.naturalKey("number")
		.detail(PublishedView.reading(EpisodeRow.class, StoryRowMapper::toDomain))
		.summary(PublishedView.reading(EpisodeSummaryRow.class, StoryRowMapper::toDomain))
		.filters(Map.of(ARC_FILTER, ContentFilter.onContent("arc_id", "ARC")))
		.build();

	/**
	 * The release days of the volumes, chapters and episodes online, by country (plan
	 * S13).
	 */
	public static final PublishedReleases VOLUME_RELEASES = new PublishedReleases("published.volume_release",
			"volume_id", "release_date");

	public static final PublishedReleases CHAPTER_RELEASES = new PublishedReleases("published.chapter_release",
			"chapter_id", "release_date");

	public static final PublishedReleases EPISODE_BROADCASTS = new PublishedReleases("published.episode_broadcast",
			"episode_id", "broadcast_date");

	private PublishedViews() {
	}

	/**
	 * The arcs of the chapters online of a volume: not asked for in the URL, only for the
	 * volume detail (story structure plan S7).
	 */
	private static ContentFilter arcsOfVolume() {
		return new ContentFilter("id IN (SELECT arc_id FROM published.chapter WHERE volume_id = :%s)",
				"id IN (SELECT arc_id FROM published.chapter WHERE volume_id = (SELECT content_id"
						+ " FROM published.content_slug WHERE entity_type = 'VOLUME' AND slug = :%s))");
	}

}
