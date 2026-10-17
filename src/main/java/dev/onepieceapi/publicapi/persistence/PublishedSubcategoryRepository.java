package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.AsOfChapter;
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

	/**
	 * Those of one type in one language, in the order of the type's version (S4); with
	 * the spoiler filter, only those met by then (plan of the timeline, TL6).
	 */
	public List<DevilFruitSubcategory> findOfType(UUID typeId, String language, AsOfChapter asOfChapter) {
		String met = asOfChapter.applies() ? " AND " + PublishedViews.SUBCATEGORY_MET : "";
		return this.jdbc
			.sql("SELECT id, name, description" + PublishedViews.FIRST_APPEARANCE_COLUMNS
					+ " FROM published.devil_fruit_type_subcategory WHERE type_id = :type AND language = :language"
					+ met + " ORDER BY position")
			.param("type", typeId)
			.param("language", language)
			.param("asOfChapter", asOfChapter.number())
			.query(DevilFruitSubcategoryRow.class)
			.list()
			.stream()
			.map(PublishedRowMapper::toDomain)
			.toList();
	}

}
