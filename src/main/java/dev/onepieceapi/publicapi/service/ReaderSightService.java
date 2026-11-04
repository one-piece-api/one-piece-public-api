package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.readertime.Point;
import dev.onepieceapi.publicapi.domain.ReaderKnowledge;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.persistence.PublishedBountyChainRepository;
import dev.onepieceapi.publicapi.persistence.PublishedMeetingRepository;
import dev.onepieceapi.publicapi.persistence.PublishedRevealRepository;
import dev.onepieceapi.publicapi.persistence.PublishedRevisionRepository;
import dev.onepieceapi.readertime.ReaderPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What a reader at N sees (reveals plan R7 D1, S10, R7c): where the reader meets each
 * content and what the reader may know of its revealable fields. Both depend only on what
 * is online, not on N, so they are read once per language and published revision and
 * shared by every request until the revision moves; the position is the request's own.
 * <p>
 * Called inside the request's snapshot transaction ({@link PublishedSnapshotService}):
 * the revision read here and the data read for it are then the same snapshot, so a cached
 * entry never mixes two revisions. A request holding an older snapshot than the cache
 * reads for itself and leaves the newer entry alone.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ReaderSightService {

	private final PublishedMeetingRepository meetings;

	private final PublishedRevealRepository reveals;

	private final PublishedBountyChainRepository bounties;

	private final PublishedRevisionRepository revisions;

	private final Map<String, Online> online = new ConcurrentHashMap<>();

	public ReaderSight at(ReaderPosition position, String language) {
		Online known = onlineAt(this.revisions.current(), language);
		return ReaderSight.of(position, known.meetings(), known.knowledge());
	}

	/**
	 * What is online in the language at the revision. The load runs under the language's
	 * entry lock, so concurrent requests after a change wait for one load instead of each
	 * running their own.
	 */
	private Online onlineAt(long revision, String language) {
		Online[] older = new Online[1];
		Online current = this.online.compute(language, (key, cached) -> {
			if (cached != null && cached.revision() == revision) {
				return cached;
			}
			Online loaded = load(revision, language);
			if (cached != null && cached.revision() > revision) {
				older[0] = loaded;
				return cached;
			}
			return loaded;
		});
		return (older[0] != null) ? older[0] : current;
	}

	private Online load(long revision, String language) {
		ReaderKnowledge knowledge = ReaderKnowledge.builder()
			.characters(this.reveals.findAllCharacterReveals(language))
			.devilFruits(this.reveals.findAllDevilFruitReveals(language))
			.bounties(this.reveals.findAllBountyReveals(language))
			.successors(this.bounties.findSuccessors(language))
			.build();
		return new Online(revision, Map.copyOf(this.meetings.findAll(language)), knowledge);
	}

	/** Everything online in one language at one revision; read-only once built. */
	private record Online(long revision, Map<UUID, Point> meetings, ReaderKnowledge knowledge) {
	}

}
