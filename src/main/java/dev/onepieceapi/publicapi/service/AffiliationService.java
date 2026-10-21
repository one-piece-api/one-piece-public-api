package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Affiliation;
import dev.onepieceapi.publicapi.domain.AffiliationSummary;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/** The affiliations online: the generic rules, bound to their view; no relation yet. */
@Service
public class AffiliationService extends PublishedContentService<Affiliation, AffiliationSummary> {

	AffiliationService(LanguageService languages, PublishedContentRepository repository) {
		super(languages, repository, PublishedViews.AFFILIATION);
	}

}
