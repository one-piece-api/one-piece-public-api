package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.domain.Saga;
import dev.onepieceapi.publicapi.domain.SagaSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedStoryDayRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The sagas online: the generic rules, bound to their view, and the detail completed with
 * the arcs of the saga (story structure plan S7); its chapters and episodes are reached
 * through the filter on their own sections.
 */
@Service
public class SagaService extends PublishedContentService<Saga, SagaSummary> {

	private final PublishedContentRepository repository;

	private final PublishedStoryDayRepository days;

	SagaService(LanguageService languages, PublishedContentRepository repository, ReaderSightService sights,
			PublishedStoryDayRepository days) {
		super(languages, repository, sights, PublishedViews.SAGA);
		this.repository = repository;
		this.days = days;
	}

	@Override
	protected Saga complete(Saga saga) {
		var ofThisSaga = new ContentFilterValue(PublishedViews.SAGA_FILTER, saga.id().toString());
		return saga.withArcs(this.repository.listAll(PublishedViews.ARC, saga.language(), ofThisSaga));
	}

	/** Its spans count only the chapters or episodes the reader has reached. */
	@Override
	protected Saga completeFor(Saga saga, ReaderSight sight) {
		return super.completeFor(saga, sight).toBuilder()
			.chapters(sight.chapterSpan(this.days.chaptersOfSaga(saga.id())))
			.episodes(sight.episodeSpan(this.days.episodesOfSaga(saga.id())))
			.build();
	}

}
