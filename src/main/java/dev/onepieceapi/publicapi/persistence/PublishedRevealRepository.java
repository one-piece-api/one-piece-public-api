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

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The initial knowledge and the reveals of the contents online (workflow 3.7.1; V42
 * views), in the language of the path, the reveals in the version's order: of one
 * content, or of every content of an entity (what the reader may know at N, reveals plan
 * R7 S10), one query for each. A value a reveal names without one reveals the current
 * value.
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedRevealRepository {

	/** No condition on the content: every content of the entity. */
	private static final String EVERY_CONTENT = "TRUE";

	private static final String CHARACTER_INITIAL = """
			SELECT k.character_id AS content_id, NULL AS fields, NULL AS title, n.name, k.romaji, k.slug,
			       k.japanese_name, k.status, NULL AS revealed_chapter, NULL AS revealed_episode
			FROM published.character_initial_knowledge k
			         LEFT JOIN published.character_initial_knowledge_name n
			                   ON n.character_id = k.character_id AND n.language = :language
			WHERE %s""";

	private static final String CHARACTER_REVEALS = """
			SELECT r.character_id AS content_id, array_to_string(r.fields, ',') AS fields, t.title, t.name, r.romaji,
			       r.slug, r.japanese_name, r.status, r.revealed_chapter, r.revealed_episode
			FROM published.character_reveal r
			         LEFT JOIN published.character_reveal_translation t
			                   ON t.character_id = r.character_id AND t.position = r.position
			                       AND t.language = :language
			WHERE %s
			ORDER BY r.character_id, r.position""";

	/**
	 * The type and the subcategory named in the language: their names are null when not
	 * written in it.
	 */
	private static final String DEVIL_FRUIT_INITIAL = """
			SELECT k.devil_fruit_id AS content_id, NULL AS fields, NULL AS title, n.name, k.romaji, k.slug, k.type_id,
			       ty.slug AS type_slug, ty.romaji AS type_romaji, ty.name AS type_name, k.subcategory_id,
			       s.name AS subcategory_name, s.description AS subcategory_description, NULL AS revealed_chapter,
			       NULL AS revealed_episode
			FROM published.devil_fruit_initial_knowledge k
			         LEFT JOIN published.devil_fruit_initial_knowledge_name n
			                   ON n.devil_fruit_id = k.devil_fruit_id AND n.language = :language
			         LEFT JOIN published.devil_fruit_type ty ON ty.id = k.type_id AND ty.language = :language
			         LEFT JOIN published.devil_fruit_type_subcategory s
			                   ON s.id = k.subcategory_id AND s.language = :language
			WHERE %s""";

	private static final String DEVIL_FRUIT_REVEALS = """
			SELECT r.devil_fruit_id AS content_id, array_to_string(r.fields, ',') AS fields, t.title, t.name, r.romaji,
			       r.slug, r.type_id, ty.slug AS type_slug, ty.romaji AS type_romaji, ty.name AS type_name,
			       r.subcategory_id, s.name AS subcategory_name, s.description AS subcategory_description,
			       r.revealed_chapter, r.revealed_episode
			FROM published.devil_fruit_reveal r
			         LEFT JOIN published.devil_fruit_reveal_translation t
			                   ON t.devil_fruit_id = r.devil_fruit_id AND t.position = r.position
			                       AND t.language = :language
			         LEFT JOIN published.devil_fruit_type ty ON ty.id = r.type_id AND ty.language = :language
			         LEFT JOIN published.devil_fruit_type_subcategory s
			                   ON s.id = r.subcategory_id AND s.language = :language
			WHERE %s
			ORDER BY r.devil_fruit_id, r.position""";

	private static final String BOUNTY_INITIAL = """
			SELECT bounty_id AS content_id, NULL AS fields, NULL AS title, inactive, NULL AS revealed_chapter,
			       NULL AS revealed_episode
			FROM published.bounty_initial_knowledge
			WHERE %s""";

	private static final String BOUNTY_REVEALS = """
			SELECT r.bounty_id AS content_id, array_to_string(r.fields, ',') AS fields, t.title, r.inactive,
			       r.revealed_chapter, r.revealed_episode
			FROM published.bounty_reveal r
			         LEFT JOIN published.bounty_reveal_translation t
			                   ON t.bounty_id = r.bounty_id AND t.position = r.position AND t.language = :language
			WHERE %s
			ORDER BY r.bounty_id, r.position""";

	private final JdbcClient jdbc;

	public FieldReveals<CharacterField, CharacterValues> findCharacterReveals(UUID characterId, String language) {
		return findCharacterReveals("k.character_id = :id", "r.character_id = :id", characterId, language)
			.getOrDefault(characterId, FieldReveals.none());
	}

	/** Every character online with reveals, by id. */
	public Map<UUID, FieldReveals<CharacterField, CharacterValues>> findAllCharacterReveals(String language) {
		return findCharacterReveals(EVERY_CONTENT, EVERY_CONTENT, null, language);
	}

	public FieldReveals<DevilFruitField, DevilFruitValues> findDevilFruitReveals(UUID devilFruitId, String language) {
		return findDevilFruitReveals("k.devil_fruit_id = :id", "r.devil_fruit_id = :id", devilFruitId, language)
			.getOrDefault(devilFruitId, FieldReveals.none());
	}

	/** Every Devil Fruit online with reveals, by id. */
	public Map<UUID, FieldReveals<DevilFruitField, DevilFruitValues>> findAllDevilFruitReveals(String language) {
		return findDevilFruitReveals(EVERY_CONTENT, EVERY_CONTENT, null, language);
	}

	public FieldReveals<BountyField, BountyValues> findBountyReveals(UUID bountyId, String language) {
		return findBountyReveals("bounty_id = :id", "r.bounty_id = :id", bountyId, language).getOrDefault(bountyId,
				FieldReveals.none());
	}

	/** Every bounty online with reveals, by id. */
	public Map<UUID, FieldReveals<BountyField, BountyValues>> findAllBountyReveals(String language) {
		return findBountyReveals(EVERY_CONTENT, EVERY_CONTENT, null, language);
	}

	private Map<UUID, FieldReveals<CharacterField, CharacterValues>> findCharacterReveals(String initialCondition,
			String revealCondition, UUID id, String language) {
		return RevealRowMapper.toCharacterReveals(
				rows(CHARACTER_INITIAL.formatted(initialCondition), id, language, CharacterValuesRow.class),
				rows(CHARACTER_REVEALS.formatted(revealCondition), id, language, CharacterValuesRow.class));
	}

	private Map<UUID, FieldReveals<DevilFruitField, DevilFruitValues>> findDevilFruitReveals(String initialCondition,
			String revealCondition, UUID id, String language) {
		return RevealRowMapper.toDevilFruitReveals(
				rows(DEVIL_FRUIT_INITIAL.formatted(initialCondition), id, language, DevilFruitValuesRow.class),
				rows(DEVIL_FRUIT_REVEALS.formatted(revealCondition), id, language, DevilFruitValuesRow.class));
	}

	private Map<UUID, FieldReveals<BountyField, BountyValues>> findBountyReveals(String initialCondition,
			String revealCondition, UUID id, String language) {
		return RevealRowMapper.toBountyReveals(
				rows(BOUNTY_INITIAL.formatted(initialCondition), id, language, BountyValuesRow.class),
				rows(BOUNTY_REVEALS.formatted(revealCondition), id, language, BountyValuesRow.class));
	}

	/**
	 * @param id the content asked for; null for every content
	 */
	private <R> List<R> rows(String sql, UUID id, String language, Class<R> rowType) {
		var statement = this.jdbc.sql(sql).param("language", language);
		return ((id != null) ? statement.param("id", id) : statement).query(rowType).list();
	}

}
