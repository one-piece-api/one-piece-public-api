package dev.onepieceapi.publicapi.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The number that moves whenever what is online changes: {@code published.revision}. */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedRevisionRepository {

	private final JdbcClient jdbc;

	public long current() {
		return this.jdbc.sql("SELECT revision FROM published.revision").query(Long.class).single();
	}

}
