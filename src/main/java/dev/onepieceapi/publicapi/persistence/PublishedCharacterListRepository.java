package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.CharacterImage;
import dev.onepieceapi.publicapi.domain.FruitUse;
import dev.onepieceapi.publicapi.domain.FruitUser;
import dev.onepieceapi.publicapi.domain.HeldBounty;
import dev.onepieceapi.publicapi.domain.Membership;
import dev.onepieceapi.publicapi.domain.Relative;
import dev.onepieceapi.publicapi.domain.SpeciesEntry;
import dev.onepieceapi.publicapi.domain.TextEntry;
import dev.onepieceapi.publicapi.persistence.mapper.BountyRowMapper;
import dev.onepieceapi.publicapi.persistence.mapper.CharacterRowMapper;
import dev.onepieceapi.publicapi.persistence.row.FruitUseRow;
import dev.onepieceapi.publicapi.persistence.row.GalleryImageRow;
import dev.onepieceapi.publicapi.persistence.row.HeldBountyRow;
import dev.onepieceapi.publicapi.persistence.row.MembershipRow;
import dev.onepieceapi.publicapi.persistence.row.RelativeRow;
import dev.onepieceapi.publicapi.persistence.row.SpeciesReferenceRow;
import dev.onepieceapi.publicapi.persistence.row.TextEntryRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The lists of the characters online, each read from its child view in the version's
 * order (Character plan D14): one query per list, for the detail only; the bounties come
 * from the bounty view.
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedCharacterListRepository {

	/**
	 * A window's columns (workflow 3.7.2), as every child view of the reader axis has
	 * them.
	 */
	private static final String WINDOW_COLUMNS = "discovered_chapter, discovered_episode, ended_chapter, ended_episode";

	private static final String GALLERY_QUERY = """
			SELECT g.character_id, g.image_id, g.kind, c.caption, g.discovered_chapter, g.discovered_episode,
			       g.ended_chapter, g.ended_episode
			FROM published.character_gallery g
			         LEFT JOIN published.character_gallery_caption c
			                   ON c.character_id = g.character_id AND c.position = g.position
			                       AND c.language = :language""";

	private final JdbcClient jdbc;

	/** Those written in the language: an epithet without text in it is left out. */
	public List<TextEntry> findEpithets(UUID characterId, String language) {
		return this.jdbc
			.sql("SELECT text AS value, " + WINDOW_COLUMNS + " FROM published.character_epithet"
					+ " WHERE character_id = :id AND language = :language ORDER BY position")
			.param("id", characterId)
			.param("language", language)
			.query(TextEntryRow.class)
			.list()
			.stream()
			.map(CharacterRowMapper::toDomain)
			.toList();
	}

	public List<TextEntry> findAliases(UUID characterId) {
		return this.jdbc
			.sql("SELECT alias AS value, " + WINDOW_COLUMNS
					+ " FROM published.character_alias WHERE character_id = :id ORDER BY position")
			.param("id", characterId)
			.query(TextEntryRow.class)
			.list()
			.stream()
			.map(CharacterRowMapper::toDomain)
			.toList();
	}

	/**
	 * Its species online, named in the language: one not written in it is left out, as a
	 * subcategory.
	 */
	public List<SpeciesEntry> findSpecies(UUID characterId, String language) {
		return this.jdbc.sql("""
				SELECT s.id, s.slug, s.name, cs.discovered_chapter, cs.discovered_episode, cs.ended_chapter,
				       cs.ended_episode
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
	 * order, each with its periods in order (workflow 3.4.7): an affiliation not written
	 * in it is left out, a role not written in it is null.
	 */
	public List<Membership> findAffiliations(UUID characterId, String language) {
		var periods = this.jdbc.sql("""
				SELECT ca.position AS membership_position, a.id, a.slug, a.name, a.kind, r.role, p.marine_rank,
				       p.discovered_chapter, p.discovered_episode, p.ended_chapter, p.ended_episode
				FROM published.character_affiliation ca
				         JOIN published.affiliation a ON a.id = ca.affiliation_id AND a.language = :language
				         JOIN published.character_affiliation_period p
				              ON p.character_id = ca.character_id AND p.membership_position = ca.position
				         LEFT JOIN published.character_affiliation_period_role r
				                   ON r.character_id = p.character_id
				                       AND r.membership_position = p.membership_position
				                       AND r.position = p.position AND r.language = :language
				WHERE ca.character_id = :id
				ORDER BY ca.position, p.position""")
			.param("id", characterId)
			.param("language", language)
			.query(MembershipRow.class)
			.list();
		return CharacterRowMapper.toMemberships(periods);
	}

	/**
	 * How many characters online belong now to this affiliation: those who left it not
	 * counted.
	 */
	public long countMembers(UUID affiliationId) {
		return this.jdbc
			.sql("SELECT count(DISTINCT character_id) FROM published.character_affiliation ca"
					+ " WHERE affiliation_id = :id AND " + PublishedViews.currentMembership("ca"))
			.param("id", affiliationId)
			.query(Long.class)
			.single();
	}

	/**
	 * The characters online that belong now to this affiliation, each once: those a
	 * reader at N has met are counted (reveals plan R7 D1).
	 */
	public List<UUID> findCurrentMemberIds(UUID affiliationId) {
		return this.jdbc
			.sql("SELECT DISTINCT character_id FROM published.character_affiliation ca"
					+ " WHERE affiliation_id = :id AND " + PublishedViews.currentMembership("ca"))
			.param("id", affiliationId)
			.query(UUID.class)
			.list();
	}

	/**
	 * Its Devil Fruits online, named in the language and in the version's order: a fruit
	 * not written in it is left out.
	 */
	public List<FruitUse> findDevilFruits(UUID characterId, String language) {
		return this.jdbc.sql("""
				SELECT f.id, f.slug, f.name, cf.discovered_chapter, cf.discovered_episode, cf.ended_chapter,
				       cf.ended_episode
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
				SELECT c.id, c.slug, c.name, cf.discovered_chapter, cf.discovered_episode, cf.ended_chapter,
				       cf.ended_episode
				FROM published.character_devil_fruit cf
				         JOIN published.character c ON c.id = cf.character_id AND c.language = :language
				WHERE cf.devil_fruit_id = :id
				ORDER BY NOT (cf.ended_chapter IS NULL AND cf.ended_episode IS NULL), c.name, c.id""")
			.param("id", devilFruitId)
			.param("language", language)
			.query(FruitUseRow.class)
			.list()
			.stream()
			.map(CharacterRowMapper::toFruitUser)
			.toList();
	}

	/**
	 * The relatives it stores, named in the language and in the version's order, with the
	 * kind as stored: a relative not written in it is left out.
	 */
	public List<Relative> findRelatives(UUID characterId, String language) {
		return this.jdbc.sql("""
				SELECT c.id, c.slug, c.name, c.gender, cr.kind, cr.discovered_chapter, cr.discovered_episode,
				       cr.ended_chapter, cr.ended_episode
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
				SELECT c.id, c.slug, c.name, c.gender, cr.kind, cr.discovered_chapter, cr.discovered_episode,
				       cr.ended_chapter, cr.ended_episode
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

	/**
	 * Its gallery, in the version's order, each caption in the language (null when not
	 * written in it).
	 */
	public List<CharacterImage> findGallery(UUID characterId, String language) {
		return this.jdbc.sql(GALLERY_QUERY + " WHERE g.character_id = :id ORDER BY g.position")
			.param("id", characterId)
			.param("language", language)
			.query(GalleryImageRow.class)
			.list()
			.stream()
			.map(CharacterRowMapper::toDomain)
			.toList();
	}

	/**
	 * The galleries of several characters in one query, each in the version's order: for
	 * the main portrait of a page of them (workflow 3.5.1).
	 */
	public Map<UUID, List<CharacterImage>> findGalleries(Collection<UUID> characterIds, String language) {
		if (characterIds.isEmpty()) {
			return Map.of();
		}
		return this.jdbc.sql(GALLERY_QUERY + " WHERE g.character_id IN (:ids) ORDER BY g.character_id, g.position")
			.param("ids", characterIds)
			.param("language", language)
			.query(GalleryImageRow.class)
			.list()
			.stream()
			.collect(Collectors.groupingBy(GalleryImageRow::characterId,
					Collectors.mapping(CharacterRowMapper::toDomain, Collectors.toList())));
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
