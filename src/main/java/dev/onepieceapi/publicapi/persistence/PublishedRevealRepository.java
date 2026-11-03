package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.BountyField;
import dev.onepieceapi.publicapi.domain.BountyValues;
import dev.onepieceapi.publicapi.domain.CharacterField;
import dev.onepieceapi.publicapi.domain.CharacterValues;
import dev.onepieceapi.publicapi.domain.DevilFruitField;
import dev.onepieceapi.publicapi.domain.DevilFruitValues;
import dev.onepieceapi.publicapi.domain.FieldReveals;
import dev.onepieceapi.publicapi.persistence.mapper.RevealRowMapper;
import dev.onepieceapi.publicapi.persistence.row.BountyValuesRow;
import dev.onepieceapi.publicapi.persistence.row.CharacterValuesRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitValuesRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * The initial knowledge and the reveals of the contents online (workflow 3.7.1; V42
 * views): one query for each, in the language of the path, the reveals in the version's
 * order. A value a reveal names without one reveals the current value.
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedRevealRepository {

	private final JdbcClient jdbc;

	public FieldReveals<CharacterField, CharacterValues> findCharacterReveals(UUID characterId, String language) {
		var initial = this.jdbc.sql("""
				SELECT NULL AS fields, NULL AS title, n.name, k.romaji, k.slug, k.japanese_name, k.status,
				       NULL AS revealed_chapter, NULL AS revealed_episode
				FROM published.character_initial_knowledge k
				         LEFT JOIN published.character_initial_knowledge_name n
				                   ON n.character_id = k.character_id AND n.language = :language
				WHERE k.character_id = :id""")
			.param("id", characterId)
			.param("language", language)
			.query(CharacterValuesRow.class)
			.optional();
		var reveals = this.jdbc.sql("""
				SELECT array_to_string(r.fields, ',') AS fields, t.title, t.name, r.romaji, r.slug, r.japanese_name,
				       r.status, r.revealed_chapter, r.revealed_episode
				FROM published.character_reveal r
				         LEFT JOIN published.character_reveal_translation t
				                   ON t.character_id = r.character_id AND t.position = r.position
				                       AND t.language = :language
				WHERE r.character_id = :id
				ORDER BY r.position""")
			.param("id", characterId)
			.param("language", language)
			.query(CharacterValuesRow.class)
			.list();
		return RevealRowMapper.toCharacterReveals(initial, reveals);
	}

	/**
	 * The type and the subcategory named in the language: their names are null when not
	 * written in it.
	 */
	public FieldReveals<DevilFruitField, DevilFruitValues> findDevilFruitReveals(UUID devilFruitId, String language) {
		var initial = this.jdbc.sql("""
				SELECT NULL AS fields, NULL AS title, n.name, k.romaji, k.slug, k.type_id, ty.slug AS type_slug,
				       ty.romaji AS type_romaji, ty.name AS type_name, k.subcategory_id, s.name AS subcategory_name,
				       s.description AS subcategory_description, NULL AS revealed_chapter, NULL AS revealed_episode
				FROM published.devil_fruit_initial_knowledge k
				         LEFT JOIN published.devil_fruit_initial_knowledge_name n
				                   ON n.devil_fruit_id = k.devil_fruit_id AND n.language = :language
				         LEFT JOIN published.devil_fruit_type ty ON ty.id = k.type_id AND ty.language = :language
				         LEFT JOIN published.devil_fruit_type_subcategory s
				                   ON s.id = k.subcategory_id AND s.language = :language
				WHERE k.devil_fruit_id = :id""")
			.param("id", devilFruitId)
			.param("language", language)
			.query(DevilFruitValuesRow.class)
			.optional();
		var reveals = this.jdbc.sql("""
				SELECT array_to_string(r.fields, ',') AS fields, t.title, t.name, r.romaji, r.slug, r.type_id,
				       ty.slug AS type_slug, ty.romaji AS type_romaji, ty.name AS type_name, r.subcategory_id,
				       s.name AS subcategory_name, s.description AS subcategory_description, r.revealed_chapter,
				       r.revealed_episode
				FROM published.devil_fruit_reveal r
				         LEFT JOIN published.devil_fruit_reveal_translation t
				                   ON t.devil_fruit_id = r.devil_fruit_id AND t.position = r.position
				                       AND t.language = :language
				         LEFT JOIN published.devil_fruit_type ty ON ty.id = r.type_id AND ty.language = :language
				         LEFT JOIN published.devil_fruit_type_subcategory s
				                   ON s.id = r.subcategory_id AND s.language = :language
				WHERE r.devil_fruit_id = :id
				ORDER BY r.position""")
			.param("id", devilFruitId)
			.param("language", language)
			.query(DevilFruitValuesRow.class)
			.list();
		return RevealRowMapper.toDevilFruitReveals(initial, reveals);
	}

	public FieldReveals<BountyField, BountyValues> findBountyReveals(UUID bountyId, String language) {
		var initial = this.jdbc.sql("""
				SELECT NULL AS fields, NULL AS title, inactive, NULL AS revealed_chapter, NULL AS revealed_episode
				FROM published.bounty_initial_knowledge
				WHERE bounty_id = :id""").param("id", bountyId).query(BountyValuesRow.class).optional();
		var reveals = this.jdbc.sql("""
				SELECT array_to_string(r.fields, ',') AS fields, t.title, r.inactive, r.revealed_chapter,
				       r.revealed_episode
				FROM published.bounty_reveal r
				         LEFT JOIN published.bounty_reveal_translation t
				                   ON t.bounty_id = r.bounty_id AND t.position = r.position AND t.language = :language
				WHERE r.bounty_id = :id
				ORDER BY r.position""")
			.param("id", bountyId)
			.param("language", language)
			.query(BountyValuesRow.class)
			.list();
		return RevealRowMapper.toBountyReveals(initial, reveals);
	}

}
