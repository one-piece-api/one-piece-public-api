package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.DevilFruitSubcategory;
import dev.onepieceapi.publicapi.persistence.mapper.PublishedRowMapper;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitSubcategoryRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * The subcategories of the types online, read through
 * {@code published.devil_fruit_type_subcategory} (subcategories plan S9).
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedSubcategoryRepository {

	private final JdbcClient jdbc;

	/** Those of one type in one language, in the order of the type's version (S4). */
	public List<DevilFruitSubcategory> findOfType(UUID typeId, String language) {
		return this.jdbc
			.sql("SELECT id, name, description FROM published.devil_fruit_type_subcategory"
					+ " WHERE type_id = :type AND language = :language ORDER BY position")
			.param("type", typeId)
			.param("language", language)
			.query(DevilFruitSubcategoryRow.class)
			.list()
			.stream()
			.map(PublishedRowMapper::toDomain)
			.toList();
	}

}
