package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.BountySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * The chain of a bounty online (Bounty plan D3, D5): the bounties it replaces and those
 * that replace it, read in one query walking {@code previous_id} both ways from it. The
 * depth is the position in the chain (older below zero, newer above), so the order is the
 * chain's, not the amounts'. Only rows in the language are walked: a link not written in
 * it ends the chain there, as a content not written in a language does not exist in it.
 * {@code CYCLE} stops the walk should the data ever loop; content-service refuses a cycle
 * online.
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedBountyChainRepository {

	private static final String CHAIN = """
			WITH RECURSIVE older (id, previous_id, depth) AS (
			    SELECT id, previous_id, 0 FROM published.bounty WHERE language = :language AND id = :id
			    UNION ALL
			    SELECT b.id, b.previous_id, o.depth - 1
			    FROM published.bounty b JOIN older o ON b.id = o.previous_id
			    WHERE b.language = :language
			) CYCLE id SET looped USING path,
			newer (id, depth) AS (
			    SELECT id, 0 FROM published.bounty WHERE language = :language AND id = :id
			    UNION ALL
			    SELECT b.id, n.depth + 1
			    FROM published.bounty b JOIN newer n ON b.previous_id = n.id
			    WHERE b.language = :language
			) CYCLE id SET looped USING path,
			chain (id, depth) AS (
			    SELECT id, depth FROM older WHERE NOT looped
			    UNION
			    SELECT id, depth FROM newer WHERE NOT looped
			)
			SELECT %s FROM published.bounty JOIN chain USING (id) WHERE language = :language ORDER BY depth DESC""";

	private final JdbcClient jdbc;

	/** Newest first, the bounty itself included; empty when it is not online. */
	public List<BountySummary> findChain(String language, UUID bountyId) {
		return this.jdbc.sql(CHAIN.formatted(PublishedViews.BOUNTY.summaryColumns()))
			.param("language", language)
			.param("id", bountyId)
			.query(PublishedViews.BOUNTY.summary())
			.list();
	}

}
