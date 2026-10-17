package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.StorySpan;
import dev.onepieceapi.publicapi.persistence.mapper.StoryRowMapper;
import dev.onepieceapi.publicapi.persistence.row.StorySpanRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * The chapters of a saga, arc or volume as the reader knows them at a chapter (plan of
 * the timeline, T8): the span the views store, cut at that chapter. Computed here on the
 * published views, so no new view is needed for it.
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedChapterSpanRepository {

	/** Which chapters belong to the parent {@code :id}. */
	public enum Parent {

		SAGA("c.arc_id IN (SELECT id FROM published.arc WHERE saga_id = :id)"), ARC("c.arc_id = :id"),
		VOLUME("c.volume_id = :id");

		private final String condition;

		Parent(String condition) {
			this.condition = condition;
		}

	}

	/** One row per chapter online, whatever the language; the day is the Japanese one. */
	private static final String SPAN = "SELECT count(DISTINCT c.id)::int AS count, min(c.number) AS first_number,"
			+ " max(c.number) AS last_number, min(r.release_date) AS first_date, max(r.release_date) AS last_date"
			+ " FROM (SELECT DISTINCT id, number, arc_id, volume_id FROM published.chapter) c"
			+ " LEFT JOIN published.chapter_release r ON r.chapter_id = c.id AND r.country = 'JP'"
			+ " WHERE c.number <= :upTo AND ";

	private final JdbcClient jdbc;

	public StorySpan chaptersUpTo(Parent parent, UUID id, int upTo) {
		var row = this.jdbc.sql(SPAN + parent.condition)
			.param("id", id)
			.param("upTo", upTo)
			.query(StorySpanRow.class)
			.single();
		return StoryRowMapper.toDomain(row);
	}

}
