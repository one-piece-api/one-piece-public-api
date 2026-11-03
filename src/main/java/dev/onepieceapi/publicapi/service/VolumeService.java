package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.domain.Volume;
import dev.onepieceapi.publicapi.domain.VolumeSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedReleaseRepository;
import dev.onepieceapi.publicapi.persistence.PublishedStoryDayRepository;
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

	private final PublishedStoryDayRepository days;

	VolumeService(LanguageService languages, PublishedContentRepository repository, ReaderSightService sights,
			PublishedReleaseRepository releases, PublishedStoryDayRepository days) {
		super(languages, repository, sights, PublishedViews.VOLUME);
		this.repository = repository;
		this.releases = releases;
		this.days = days;
	}

	@Override
	protected Volume complete(Volume volume) {
		var ofThisVolume = new ContentFilterValue(PublishedViews.VOLUME_FILTER, volume.id().toString());
		return volume.withArcs(this.repository.listAll(PublishedViews.ARC, volume.language(), ofThisVolume))
			.withReleases(this.releases.findOf(PublishedViews.VOLUME_RELEASES, volume.id()));
	}

	/** Its span counts only the chapters the reader has reached. */
	@Override
	protected Volume completeFor(Volume volume, ReaderSight sight) {
		return super.completeFor(volume, sight).toBuilder()
			.chapters(sight.chapterSpan(this.days.chaptersOfVolume(volume.id())))
			.build();
	}

}
