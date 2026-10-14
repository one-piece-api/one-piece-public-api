package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.Release;
import dev.onepieceapi.publicapi.persistence.mapper.StoryRowMapper;
import dev.onepieceapi.publicapi.persistence.row.ReleaseRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * The release and broadcast days of the contents online, one per country, read through
 * the view a {@link PublishedReleases} names (story structure plan S13).
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedReleaseRepository {

	private final JdbcClient jdbc;

	/** Every country of one content, by day then country: no country is left out. */
	public List<Release> findOf(PublishedReleases releases, UUID contentId) {
		return this.jdbc
			.sql("SELECT country, " + releases.dateColumn() + " AS date FROM " + releases.view() + " WHERE "
					+ releases.idColumn() + " = :id ORDER BY date, country")
			.param("id", contentId)
			.query(ReleaseRow.class)
			.list()
			.stream()
			.map(StoryRowMapper::toDomain)
			.toList();
	}

}
