package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.CharacterEntries;
import dev.onepieceapi.publicapi.domain.Membership;
import dev.onepieceapi.publicapi.persistence.mapper.CharacterEntryRowMapper;
import dev.onepieceapi.publicapi.persistence.row.EntryRow;
import dev.onepieceapi.publicapi.persistence.row.MembershipEntryRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The lists of every character online that a list filter tests (reveals plan R7 S11): one
 * query per list, for all the characters, with the content named and its window only. The
 * contents named are those written in the language, as in the detail of a character, so a
 * filter and the detail agree.
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedCharacterEntryRepository {

	private static final String SPECIES = """
			SELECT cs.character_id, cs.species_id AS id, cs.discovered_chapter, cs.discovered_episode,
			       cs.ended_chapter, cs.ended_episode
			FROM published.character_species cs
			         JOIN published.species s ON s.id = cs.species_id AND s.language = :language
			ORDER BY cs.character_id, cs.position""";

	private static final String DEVIL_FRUITS = """
			SELECT cf.character_id, cf.devil_fruit_id AS id, cf.discovered_chapter, cf.discovered_episode,
			       cf.ended_chapter, cf.ended_episode
			FROM published.character_devil_fruit cf
			         JOIN published.devil_fruit f ON f.id = cf.devil_fruit_id AND f.language = :language
			ORDER BY cf.character_id, cf.position""";

	private static final String MEMBERSHIPS = """
			SELECT ca.character_id, ca.position AS membership_position, a.id, a.kind, p.discovered_chapter,
			       p.discovered_episode, p.ended_chapter, p.ended_episode
			FROM published.character_affiliation ca
			         JOIN published.affiliation a ON a.id = ca.affiliation_id AND a.language = :language
			         JOIN published.character_affiliation_period p
			              ON p.character_id = ca.character_id AND p.membership_position = ca.position
			%s
			ORDER BY ca.character_id, ca.position, p.position""";

	private final JdbcClient jdbc;

	/** All the lists a character filter tests. */
	public CharacterEntries findAll(String language) {
		List<EntryRow> species = this.jdbc.sql(SPECIES).param("language", language).query(EntryRow.class).list();
		List<EntryRow> devilFruits = this.jdbc.sql(DEVIL_FRUITS)
			.param("language", language)
			.query(EntryRow.class)
			.list();
		List<MembershipEntryRow> memberships = this.jdbc.sql(MEMBERSHIPS.formatted(""))
			.param("language", language)
			.query(MembershipEntryRow.class)
			.list();
		return new CharacterEntries(CharacterEntryRowMapper.toSpecies(species),
				CharacterEntryRowMapper.toMemberships(memberships), CharacterEntryRowMapper.toDevilFruits(devilFruits));
	}

	/** Only the memberships in one affiliation, to count its members. */
	public CharacterEntries findMembersOf(String language, UUID affiliationId) {
		Map<UUID, List<Membership>> memberships = CharacterEntryRowMapper
			.toMemberships(this.jdbc.sql(MEMBERSHIPS.formatted("WHERE ca.affiliation_id = :affiliation"))
				.param("language", language)
				.param("affiliation", affiliationId)
				.query(MembershipEntryRow.class)
				.list());
		return new CharacterEntries(Map.of(), memberships, Map.of());
	}

}
