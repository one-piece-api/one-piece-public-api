package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.SpeciesReference;
import dev.onepieceapi.publicapi.persistence.mapper.CharacterRowMapper;
import dev.onepieceapi.publicapi.persistence.row.SpeciesReferenceRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * The lists of the characters online, each read from its child view in the version's
 * order (Character plan D14): one query per list, for the detail only.
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedCharacterListRepository {

	private final JdbcClient jdbc;

	/** Those written in the language: an epithet without text in it is left out. */
	public List<String> findEpithets(UUID characterId, String language) {
		return this.jdbc
			.sql("SELECT text FROM published.character_epithet WHERE character_id = :id AND language = :language"
					+ " ORDER BY position")
			.param("id", characterId)
			.param("language", language)
			.query(String.class)
			.list();
	}

	public List<String> findAliases(UUID characterId) {
		return this.jdbc.sql("SELECT alias FROM published.character_alias WHERE character_id = :id ORDER BY position")
			.param("id", characterId)
			.query(String.class)
			.list();
	}

	/**
	 * Its species online, named in the language: one not written in it is left out, as a
	 * subcategory.
	 */
	public List<SpeciesReference> findSpecies(UUID characterId, String language) {
		return this.jdbc.sql("""
				SELECT s.id, s.slug, s.name
				FROM published.character_species cs
				         JOIN published.species s ON s.id = cs.species_id AND s.language = :language
				WHERE cs.character_id = :id
				ORDER BY cs.position""")
			.param("id", characterId)
			.param("language", language)
			.query(SpeciesReferenceRow.class)
			.list()
			.stream()
			.map(CharacterRowMapper::toDomain)
			.toList();
	}

}
