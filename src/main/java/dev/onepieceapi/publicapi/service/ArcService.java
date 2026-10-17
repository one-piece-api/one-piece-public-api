package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Arc;
import dev.onepieceapi.publicapi.domain.ArcSummary;
import dev.onepieceapi.publicapi.domain.AsOfChapter;
import dev.onepieceapi.publicapi.persistence.PublishedChapterSpanRepository;
import dev.onepieceapi.publicapi.persistence.PublishedChapterSpanRepository.Parent;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The arcs online: the generic rules, bound to their view; under the spoiler filter its
 * chapters stop at the reader's chapter (plan of the timeline, T8).
 */
@Service
public class ArcService extends PublishedContentService<Arc, ArcSummary> {

	private final PublishedChapterSpanRepository spans;

	ArcService(LanguageService languages, PublishedContentRepository repository, PublishedChapterSpanRepository spans) {
		super(languages, repository, PublishedViews.ARC);
		this.spans = spans;
	}

	@Override
	protected Arc complete(Arc arc, AsOfChapter asOfChapter) {
		return asOfChapter.applies()
				? arc.withChapters(this.spans.chaptersUpTo(Parent.ARC, arc.id(), asOfChapter.number())) : arc;
	}

}
