package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Affiliation;
import dev.onepieceapi.publicapi.domain.AffiliationSummary;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.persistence.PublishedCharacterEntryRepository;
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

	private final PublishedCharacterEntryRepository entries;

	AffiliationService(LanguageService languages, PublishedContentRepository repository, ReaderSightService sights,
			PublishedCharacterListRepository characters, PublishedCharacterEntryRepository entries) {
		super(languages, repository, sights, PublishedViews.AFFILIATION);
		this.characters = characters;
		this.entries = entries;
	}

	@Override
	protected Affiliation complete(Affiliation affiliation) {
		return affiliation.withMemberCount(this.characters.countMembers(affiliation.id()));
	}

	/**
	 * It counts the members the reader has met and knows to belong to it at N: the same
	 * characters the filter `affiliation` lists (reveals plan R7 S11).
	 */
	@Override
	protected Affiliation completeFor(Affiliation affiliation, ReaderSight sight) {
		long members = this.entries.findMembersOf(affiliation.language(), affiliation.id())
			.countMembers(affiliation.id(), sight);
		return affiliation.seenBy(sight).withMemberCount(members);
	}

}
