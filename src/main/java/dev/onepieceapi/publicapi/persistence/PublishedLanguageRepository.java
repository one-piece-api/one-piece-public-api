package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.Language;
import dev.onepieceapi.publicapi.persistence.mapper.PublishedRowMapper;
import dev.onepieceapi.publicapi.persistence.row.LanguageRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/** The public languages: the catalog, through {@code published.language} (plan D6). */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedLanguageRepository {

	private final JdbcClient jdbc;

	public List<Language> findAll() {
		return this.jdbc.sql("SELECT code, name FROM published.language ORDER BY code")
			.query(LanguageRow.class)
			.list()
			.stream()
			.map(PublishedRowMapper::toDomain)
			.toList();
	}

	public boolean exists(String code) {
		return this.jdbc.sql("SELECT EXISTS (SELECT 1 FROM published.language WHERE code = :code)")
			.param("code", code)
			.query(Boolean.class)
			.single();
	}

}
