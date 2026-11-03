package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Affiliation;
import dev.onepieceapi.publicapi.domain.AffiliationSummary;
import dev.onepieceapi.publicapi.domain.ReaderSight;
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

	AffiliationService(LanguageService languages, PublishedContentRepository repository, ReaderSightService sights,
			PublishedCharacterListRepository characters) {
		super(languages, repository, sights, PublishedViews.AFFILIATION);
		this.characters = characters;
	}

	@Override
	protected Affiliation complete(Affiliation affiliation) {
		return affiliation.withMemberCount(this.characters.countMembers(affiliation.id()));
	}

	/** It counts only the members the reader has met. */
	@Override
	protected Affiliation completeFor(Affiliation affiliation, ReaderSight sight) {
		var members = this.characters.findCurrentMemberIds(affiliation.id());
		return affiliation.seenBy(sight).withMemberCount(members.stream().filter(sight::sees).count());
	}

}
