package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Arc;
import dev.onepieceapi.publicapi.domain.ArcSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/** The arcs online: the generic rules, bound to their view. */
@Service
public class ArcService extends PublishedContentService<Arc, ArcSummary> {

	ArcService(LanguageService languages, PublishedContentRepository repository) {
		super(languages, repository, PublishedViews.ARC);
	}

}
