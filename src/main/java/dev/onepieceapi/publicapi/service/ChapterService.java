package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.AsOfChapter;
import dev.onepieceapi.publicapi.domain.Chapter;
import dev.onepieceapi.publicapi.domain.ChapterSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedReleaseRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The chapters online: the generic rules, bound to their view, and the detail completed
 * with the release day in every country (story structure plan S13).
 */
@Service
public class ChapterService extends PublishedContentService<Chapter, ChapterSummary> {

	private final PublishedReleaseRepository releases;

	ChapterService(LanguageService languages, PublishedContentRepository repository,
			PublishedReleaseRepository releases) {
		super(languages, repository, PublishedViews.CHAPTER);
		this.releases = releases;
	}

	@Override
	protected Chapter complete(Chapter chapter, AsOfChapter asOfChapter) {
		return chapter.withReleases(this.releases.findOf(PublishedViews.CHAPTER_RELEASES, chapter.id()));
	}

}
