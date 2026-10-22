package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Affiliation;
import dev.onepieceapi.publicapi.domain.AffiliationSummary;
import dev.onepieceapi.publicapi.persistence.PublishedCharacterListRepository;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The affiliations online: the generic rules, bound to their view, and the detail
 * completed with how many characters belong to it (Character plan C8).
 */
@Service
public class AffiliationService extends PublishedContentService<Affiliation, AffiliationSummary> {

	private final PublishedCharacterListRepository characters;

	AffiliationService(LanguageService languages, PublishedContentRepository repository,
			PublishedCharacterListRepository characters) {
		super(languages, repository, PublishedViews.AFFILIATION);
		this.characters = characters;
	}

	@Override
	protected Affiliation complete(Affiliation affiliation) {
		return affiliation.withMemberCount(this.characters.countMembers(affiliation.id()));
	}

}
