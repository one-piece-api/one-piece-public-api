package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.persistence.PublishedChapterSpanRepository.Parent;
import dev.onepieceapi.publicapi.persistence.PublishedChapterSpanRepository;
import dev.onepieceapi.publicapi.domain.AsOfChapter;
import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.Volume;
import dev.onepieceapi.publicapi.domain.VolumeSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedReleaseRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The volumes online: the generic rules, bound to their view, and the detail completed
 * with the arcs of its chapters and its release day in every country (story structure
 * plan S7, S13).
 */
@Service
public class VolumeService extends PublishedContentService<Volume, VolumeSummary> {

	private final PublishedContentRepository repository;

	private final PublishedReleaseRepository releases;

	private final PublishedChapterSpanRepository spans;

	VolumeService(LanguageService languages, PublishedContentRepository repository, PublishedReleaseRepository releases,
			PublishedChapterSpanRepository spans) {
		super(languages, repository, PublishedViews.VOLUME);
		this.repository = repository;
		this.releases = releases;
		this.spans = spans;
	}

	@Override
	protected Volume complete(Volume volume, AsOfChapter asOfChapter) {
		var ofThisVolume = new ContentFilterValue(PublishedViews.VOLUME_FILTER, volume.id().toString());
		var known = asOfChapter.applies()
				? volume.withChapters(this.spans.chaptersUpTo(Parent.VOLUME, volume.id(), asOfChapter.number()))
				: volume;
		return known.withArcs(this.repository.listAll(PublishedViews.ARC, volume.language(), ofThisVolume, asOfChapter))
			.withReleases(this.releases.findOf(PublishedViews.VOLUME_RELEASES, volume.id()));
	}

}
