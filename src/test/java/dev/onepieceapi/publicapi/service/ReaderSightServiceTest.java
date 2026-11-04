package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.readertime.Point;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.persistence.PublishedBountyChainRepository;
import dev.onepieceapi.publicapi.persistence.PublishedMeetingRepository;
import dev.onepieceapi.publicapi.persistence.PublishedRevealRepository;
import dev.onepieceapi.publicapi.persistence.PublishedRevisionRepository;
import dev.onepieceapi.readertime.ReaderPosition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReaderSightServiceTest {

	private static final UUID LUFFY = UUID.randomUUID();

	private final PublishedMeetingRepository meetings = mock(PublishedMeetingRepository.class);

	private final PublishedRevealRepository reveals = mock(PublishedRevealRepository.class);

	private final PublishedBountyChainRepository bounties = mock(PublishedBountyChainRepository.class);

	private final PublishedRevisionRepository revisions = mock(PublishedRevisionRepository.class);

	private final ReaderSightService service = new ReaderSightService(this.meetings, this.reveals, this.bounties,
			this.revisions);

	ReaderSightServiceTest() {
		when(this.meetings.findAll(anyString())).thenReturn(Map.of(LUFFY, Point.ofChapter(1)));
		when(this.reveals.findAllCharacterReveals(anyString())).thenReturn(Map.of());
		when(this.reveals.findAllDevilFruitReveals(anyString())).thenReturn(Map.of());
		when(this.reveals.findAllBountyReveals(anyString())).thenReturn(Map.of());
		when(this.bounties.findSuccessors(anyString())).thenReturn(Map.of());
	}

	@Test
	void readsWhatIsOnlineOnceWhileTheRevisionStays() {
		when(this.revisions.current()).thenReturn(7L);

		this.service.at(ReaderPosition.atChapter(1), "en");
		this.service.at(ReaderPosition.atChapter(500), "en");
		this.service.at(ReaderPosition.atEpisode(3), "en");

		verify(this.meetings, times(1)).findAll("en");
		verify(this.reveals, times(1)).findAllCharacterReveals("en");
	}

	@Test
	void givesEachRequestItsOwnPositionOverTheSameData() {
		when(this.revisions.current()).thenReturn(7L);

		ReaderSight early = this.service.at(ReaderPosition.atChapter(1), "en");
		ReaderSight late = this.service.at(ReaderPosition.atChapter(500), "en");

		assertThat(early.sees(LUFFY)).isTrue();
		assertThat(late.sees(LUFFY)).isTrue();
		assertThat(early.position()).isNotEqualTo(late.position());
	}

	@Test
	void readsAgainWhenTheRevisionMoves() {
		when(this.revisions.current()).thenReturn(7L, 8L);

		this.service.at(ReaderPosition.atChapter(1), "en");
		this.service.at(ReaderPosition.atChapter(1), "en");

		verify(this.meetings, times(2)).findAll("en");
	}

	@Test
	void keepsALanguageApartFromTheOthers() {
		when(this.revisions.current()).thenReturn(7L);

		this.service.at(ReaderPosition.atChapter(1), "en");
		this.service.at(ReaderPosition.atChapter(1), "it");
		this.service.at(ReaderPosition.atChapter(1), "en");

		verify(this.meetings, times(1)).findAll("en");
		verify(this.meetings, times(1)).findAll("it");
	}

	@Test
	void doesNotReplaceANewerEntryWithAnOlderSnapshot() {
		when(this.revisions.current()).thenReturn(8L, 7L, 8L);

		this.service.at(ReaderPosition.atChapter(1), "en");
		this.service.at(ReaderPosition.atChapter(1), "en");
		this.service.at(ReaderPosition.atChapter(1), "en");

		// 8 is read once and kept; the request still on snapshot 7 reads for itself.
		verify(this.meetings, times(2)).findAll("en");
	}

	@Test
	void loadsOnceWhenManyRequestsArriveTogether() throws Exception {
		when(this.revisions.current()).thenReturn(7L);
		int requests = 8;
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(requests);
		try {
			List<Future<ReaderSight>> sights = new ArrayList<>();
			for (int i = 0; i < requests; i++) {
				sights.add(pool.submit(() -> {
					start.await();
					return this.service.at(ReaderPosition.atChapter(1), "en");
				}));
			}
			start.countDown();
			for (Future<ReaderSight> sight : sights) {
				assertThat(sight.get().sees(LUFFY)).isTrue();
			}
		}
		finally {
			pool.shutdownNow();
		}

		verify(this.meetings, times(1)).findAll("en");
	}

}
