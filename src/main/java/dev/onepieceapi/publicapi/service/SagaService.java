package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.persistence.PublishedChapterSpanRepository.Parent;
import dev.onepieceapi.publicapi.persistence.PublishedChapterSpanRepository;
import dev.onepieceapi.publicapi.domain.AsOfChapter;
import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.Saga;
import dev.onepieceapi.publicapi.domain.SagaSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
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

	private final PublishedChapterSpanRepository spans;

	SagaService(LanguageService languages, PublishedContentRepository repository,
			PublishedChapterSpanRepository spans) {
		super(languages, repository, PublishedViews.SAGA);
		this.repository = repository;
		this.spans = spans;
	}

	@Override
	protected Saga complete(Saga saga, AsOfChapter asOfChapter) {
		var ofThisSaga = new ContentFilterValue(PublishedViews.SAGA_FILTER, saga.id().toString());
		var known = asOfChapter.applies()
				? saga.withChapters(this.spans.chaptersUpTo(Parent.SAGA, saga.id(), asOfChapter.number())) : saga;
		return known.withArcs(this.repository.listAll(PublishedViews.ARC, saga.language(), ofThisSaga, asOfChapter));
	}

}
