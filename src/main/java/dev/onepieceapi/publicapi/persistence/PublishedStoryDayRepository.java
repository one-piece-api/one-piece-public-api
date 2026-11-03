package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.StoryDay;
import dev.onepieceapi.publicapi.persistence.mapper.StoryRowMapper;
import dev.onepieceapi.publicapi.persistence.row.StoryDayRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * The chapters and episodes online of a saga, an arc or a volume, with their Japanese
 * day: what a span counts when the reader is at N (reveals plan R7 D1), the spans of the
 * views counting them all. Fixed SQL, the ids bound (plan D3).
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedStoryDayRepository {

	private static final String CHAPTERS = "SELECT DISTINCT c.number, r.release_date AS date FROM published.chapter c"
			+ " LEFT JOIN published.chapter_release r ON r.chapter_id = c.id AND r.country = 'JP' WHERE ";

	private static final String EPISODES = "SELECT DISTINCT e.number, b.broadcast_date AS date FROM published.episode e"
			+ " LEFT JOIN published.episode_broadcast b ON b.episode_id = e.id AND b.country = 'JP' WHERE ";

	private static final String OF_SAGA = "%s.arc_id IN (SELECT id FROM published.arc WHERE saga_id = :id)";

	private final JdbcClient jdbc;

	public List<StoryDay> chaptersOfSaga(UUID sagaId) {
		return read(CHAPTERS + OF_SAGA.formatted("c"), sagaId);
	}

	public List<StoryDay> episodesOfSaga(UUID sagaId) {
		return read(EPISODES + OF_SAGA.formatted("e"), sagaId);
	}

	public List<StoryDay> chaptersOfArc(UUID arcId) {
		return read(CHAPTERS + "c.arc_id = :id", arcId);
	}

	public List<StoryDay> episodesOfArc(UUID arcId) {
		return read(EPISODES + "e.arc_id = :id", arcId);
	}

	public List<StoryDay> chaptersOfVolume(UUID volumeId) {
		return read(CHAPTERS + "c.volume_id = :id", volumeId);
	}

	private List<StoryDay> read(String sql, UUID id) {
		return this.jdbc.sql(sql)
			.param("id", id)
			.query(StoryDayRow.class)
			.list()
			.stream()
			.map(StoryRowMapper::toDomain)
			.toList();
	}

}
