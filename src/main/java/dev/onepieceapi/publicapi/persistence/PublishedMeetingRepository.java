package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.persistence.mapper.ReaderAxisRowMapper;
import dev.onepieceapi.publicapi.persistence.row.MeetingRow;
import dev.onepieceapi.readertime.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Where the reader meets each content online (workflow 3.6, 3.8; reveals plan R7 D1), in
 * one read: the first appearance of an entity; the number of a chapter or an episode; the
 * first chapter of a volume; the first chapter and episode of a saga or an arc; for a
 * subcategory its own first appearance, or its type's when it has none.
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedMeetingRepository {

	private static final String FIRST_APPEARANCE = "SELECT id, first_appearance_chapter_number AS chapter,"
			+ " first_appearance_episode_number AS episode FROM published.%s WHERE language = :language";

	private static final String SUBCATEGORY = """
			SELECT s.id,
			       CASE WHEN s.first_appearance_chapter_number IS NULL AND s.first_appearance_episode_number IS NULL
			            THEN t.first_appearance_chapter_number ELSE s.first_appearance_chapter_number END,
			       CASE WHEN s.first_appearance_chapter_number IS NULL AND s.first_appearance_episode_number IS NULL
			            THEN t.first_appearance_episode_number ELSE s.first_appearance_episode_number END
			FROM published.devil_fruit_type_subcategory s
			         JOIN published.devil_fruit_type t ON t.id = s.type_id AND t.language = s.language
			WHERE s.language = :language""";

	private static final String STORY = """
			SELECT id, number, NULL::integer FROM published.chapter WHERE language = :language
			UNION ALL SELECT id, NULL::integer, number FROM published.episode WHERE language = :language
			UNION ALL SELECT id, first_chapter_number, NULL::integer FROM published.volume WHERE language = :language
			UNION ALL SELECT id, first_chapter_number, first_episode_number FROM published.saga WHERE language = :language
			UNION ALL SELECT id, first_chapter_number, first_episode_number FROM published.arc WHERE language = :language""";

	private static final String MEETINGS = Stream
		.concat(Stream.of("devil_fruit_type", "devil_fruit", "bounty", "affiliation", "species", "character")
			.map(FIRST_APPEARANCE::formatted), Stream.of(SUBCATEGORY, STORY))
		.collect(Collectors.joining(" UNION ALL "));

	private final JdbcClient jdbc;

	/** Every content online in the language, by id. */
	public Map<UUID, Point> findAll(String language) {
		return this.jdbc.sql(MEETINGS)
			.param("language", language)
			.query(MeetingRow.class)
			.list()
			.stream()
			.collect(Collectors.toMap(MeetingRow::id, ReaderAxisRowMapper::toPoint));
	}

}
