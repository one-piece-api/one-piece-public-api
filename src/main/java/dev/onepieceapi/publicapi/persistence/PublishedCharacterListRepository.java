package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.CharacterReference;
import dev.onepieceapi.publicapi.domain.FruitUse;
import dev.onepieceapi.publicapi.domain.FruitUser;
import dev.onepieceapi.publicapi.domain.HeldBounty;
import dev.onepieceapi.publicapi.domain.Membership;
import dev.onepieceapi.publicapi.domain.Relative;
import dev.onepieceapi.publicapi.domain.SpeciesReference;
import dev.onepieceapi.publicapi.persistence.mapper.BountyRowMapper;
import dev.onepieceapi.publicapi.persistence.mapper.CharacterRowMapper;
import dev.onepieceapi.publicapi.persistence.row.FruitUseRow;
import dev.onepieceapi.publicapi.persistence.row.HeldBountyRow;
import dev.onepieceapi.publicapi.persistence.row.MembershipRow;
import dev.onepieceapi.publicapi.persistence.row.RelativeRow;
import dev.onepieceapi.publicapi.persistence.row.SpeciesReferenceRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * The lists of the characters online, each read from its child view in the version's
 * order (Character plan D14): one query per list, for the detail only; the bounties come
 * from the bounty view.
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

	/**
	 * Its memberships in affiliations online, named in the language and in the version's
	 * order: an affiliation not written in it is left out, a role not written in it is
	 * null.
	 */
	public List<Membership> findAffiliations(UUID characterId, String language) {
		return this.jdbc.sql("""
				SELECT a.id, a.slug, a.name, a.kind, r.role, ca.marine_rank, ca.former
				FROM published.character_affiliation ca
				         JOIN published.affiliation a ON a.id = ca.affiliation_id AND a.language = :language
				         LEFT JOIN published.character_affiliation_role r
				                   ON r.character_id = ca.character_id AND r.position = ca.position
				                       AND r.language = :language
				WHERE ca.character_id = :id
				ORDER BY ca.position""")
			.param("id", characterId)
			.param("language", language)
			.query(MembershipRow.class)
			.list()
			.stream()
			.map(CharacterRowMapper::toDomain)
			.toList();
	}

	/**
	 * How many characters online belong now to this affiliation: those who left it not
	 * counted.
	 */
	public long countMembers(UUID affiliationId) {
		return this.jdbc
			.sql("SELECT count(DISTINCT character_id) FROM published.character_affiliation"
					+ " WHERE affiliation_id = :id AND NOT former")
			.param("id", affiliationId)
			.query(Long.class)
			.single();
	}

	/**
	 * Its Devil Fruits online, named in the language and in the version's order: a fruit
	 * not written in it is left out.
	 */
	public List<FruitUse> findDevilFruits(UUID characterId, String language) {
		return this.jdbc.sql("""
				SELECT f.id, f.slug, f.name, cf.former
				FROM published.character_devil_fruit cf
				         JOIN published.devil_fruit f ON f.id = cf.devil_fruit_id AND f.language = :language
				WHERE cf.character_id = :id
				ORDER BY cf.position""")
			.param("id", characterId)
			.param("language", language)
			.query(FruitUseRow.class)
			.list()
			.stream()
			.map(CharacterRowMapper::toFruitUse)
			.toList();
	}

	/**
	 * The characters online that have eaten this fruit, named in the language: those who
	 * have its power now first, then by name. A character not written in it is left out.
	 */
	public List<FruitUser> findUsers(UUID devilFruitId, String language) {
		return this.jdbc.sql("""
				SELECT c.id, c.slug, c.name, cf.former
				FROM published.character_devil_fruit cf
				         JOIN published.character c ON c.id = cf.character_id AND c.language = :language
				WHERE cf.devil_fruit_id = :id
				ORDER BY cf.former, c.name, c.id""")
			.param("id", devilFruitId)
			.param("language", language)
			.query(FruitUseRow.class)
			.list()
			.stream()
			.map(row -> new FruitUser(new CharacterReference(row.id(), row.slug(), row.name()), row.former()))
			.toList();
	}

	/**
	 * The relatives it stores, named in the language and in the version's order, with the
	 * kind as stored: a relative not written in it is left out.
	 */
	public List<Relative> findRelatives(UUID characterId, String language) {
		return this.jdbc.sql("""
				SELECT c.id, c.slug, c.name, c.gender, cr.kind
				FROM published.character_relative cr
				         JOIN published.character c ON c.id = cr.relative_id AND c.language = :language
				WHERE cr.character_id = :id
				ORDER BY cr.position""")
			.param("id", characterId)
			.param("language", language)
			.query(RelativeRow.class)
			.list()
			.stream()
			.map(CharacterRowMapper::toRelative)
			.toList();
	}

	/**
	 * The characters online that store it as their relative, named in the language and by
	 * name, with the kind as they store it: the caller inverts it. A character not
	 * written in the language is left out.
	 */
	public List<Relative> findNamingAsRelative(UUID characterId, String language) {
		return this.jdbc.sql("""
				SELECT c.id, c.slug, c.name, c.gender, cr.kind
				FROM published.character_relative cr
				         JOIN published.character c ON c.id = cr.character_id AND c.language = :language
				WHERE cr.relative_id = :id
				ORDER BY c.name, c.id""")
			.param("id", characterId)
			.param("language", language)
			.query(RelativeRow.class)
			.list()
			.stream()
			.map(CharacterRowMapper::toRelative)
			.toList();
	}

	/** The bounties online naming it as holder, in the language, highest amount first. */
	public List<HeldBounty> findBounties(UUID characterId, String language) {
		return this.jdbc
			.sql("SELECT id, slug, amount, validity FROM published.bounty WHERE holder_id = :id"
					+ " AND language = :language ORDER BY amount DESC, id")
			.param("id", characterId)
			.param("language", language)
			.query(HeldBountyRow.class)
			.list()
			.stream()
			.map(BountyRowMapper::toDomain)
			.toList();
	}

}
