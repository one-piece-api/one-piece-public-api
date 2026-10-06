package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Snapshot;
import dev.onepieceapi.publicapi.persistence.PublishedRevisionRepository;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublishedSnapshotServiceTest {

	private final PublishedRevisionRepository revisions = mock(PublishedRevisionRepository.class);

	private final PublishedSnapshotService service = new PublishedSnapshotService(this.revisions);

	private final AtomicInteger queries = new AtomicInteger();

	@Test
	void doesNotRunTheQueryWhenTheCallerIsCurrent() {
		when(this.revisions.current()).thenReturn(7L);

		Snapshot<String> snapshot = this.service.read(revision -> revision == 7, this::query);

		assertThat(snapshot).isEqualTo(new Snapshot.NotModified<String>(7));
		assertThat(this.queries).hasValue(0);
	}

	@Test
	void runsTheQueryOnceWhenTheCallerIsNotCurrent() {
		when(this.revisions.current()).thenReturn(8L);

		Snapshot<String> snapshot = this.service.read(revision -> false, this::query);

		assertThat(snapshot).isEqualTo(new Snapshot.Fresh<>(8L, "data"));
		assertThat(this.queries).hasValue(1);
	}

	private String query() {
		this.queries.incrementAndGet();
		return "data";
	}

}
