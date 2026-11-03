package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Arc;
import dev.onepieceapi.publicapi.domain.ArcSummary;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedStoryDayRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/** The arcs online: the generic rules, bound to their view. */
@Service
public class ArcService extends PublishedContentService<Arc, ArcSummary> {

	private final PublishedStoryDayRepository days;

	ArcService(LanguageService languages, PublishedContentRepository repository, ReaderSightService sights,
			PublishedStoryDayRepository days) {
		super(languages, repository, sights, PublishedViews.ARC);
		this.days = days;
	}

	/** Its spans count only the chapters or episodes the reader has reached. */
	@Override
	protected Arc completeFor(Arc arc, ReaderSight sight) {
		return super.completeFor(arc, sight).toBuilder()
			.chapters(sight.chapterSpan(this.days.chaptersOfArc(arc.id())))
			.episodes(sight.episodeSpan(this.days.episodesOfArc(arc.id())))
			.build();
	}

}
