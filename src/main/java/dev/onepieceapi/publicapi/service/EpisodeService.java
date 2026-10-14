package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Episode;
import dev.onepieceapi.publicapi.domain.EpisodeSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedReleaseRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The episodes online: the generic rules, bound to their view, and the detail completed
 * with the broadcast day in every country (story structure plan S13).
 */
@Service
public class EpisodeService extends PublishedContentService<Episode, EpisodeSummary> {

	private final PublishedReleaseRepository releases;

	EpisodeService(LanguageService languages, PublishedContentRepository repository,
			PublishedReleaseRepository releases) {
		super(languages, repository, PublishedViews.EPISODE);
		this.releases = releases;
	}

	@Override
	protected Episode complete(Episode episode) {
		return episode.withBroadcasts(this.releases.findOf(PublishedViews.EPISODE_BROADCASTS, episode.id()));
	}

}
