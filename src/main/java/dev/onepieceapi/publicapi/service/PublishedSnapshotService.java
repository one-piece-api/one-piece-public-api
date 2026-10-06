package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Snapshot;
import dev.onepieceapi.publicapi.persistence.PublishedRevisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.LongPredicate;
import java.util.function.Supplier;

/**
 * Reads the published revision and, only if the caller does not already have it, the data
 * - both in the same {@code REPEATABLE READ} snapshot, so the revision (the ETag) always
 * describes exactly the data returned with it (plan D13). Template Method: the caller
 * supplies the query and the "already current" test, this fixes the order and the
 * transaction around them.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedSnapshotService {

	private final PublishedRevisionRepository revisions;

	/**
	 * @param alreadyCurrent whether the caller holds this revision; when true the query
	 * is never run
	 * @param query the read; joins this transaction, so it sees the same snapshot
	 */
	@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
	public <T> Snapshot<T> read(LongPredicate alreadyCurrent, Supplier<T> query) {
		long revision = this.revisions.current();
		if (alreadyCurrent.test(revision)) {
			return new Snapshot.NotModified<>(revision);
		}
		return new Snapshot.Fresh<>(revision, query.get());
	}

}
