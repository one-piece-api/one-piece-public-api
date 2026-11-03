package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Bounty;
import dev.onepieceapi.publicapi.domain.BountySummary;
import dev.onepieceapi.publicapi.persistence.PublishedBountyChainRepository;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedRevealRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import org.springframework.stereotype.Service;

/**
 * The bounties online: the generic rules, bound to their view, and the detail completed
 * with its chain (Bounty plan D5).
 */
@Service
public class BountyService extends PublishedContentService<Bounty, BountySummary> {

	private final PublishedBountyChainRepository chains;

	private final PublishedRevealRepository reveals;

	BountyService(LanguageService languages, PublishedContentRepository repository, ReaderSightService sights,
			PublishedBountyChainRepository chains, PublishedRevealRepository reveals) {
		super(languages, repository, sights, PublishedViews.BOUNTY);
		this.chains = chains;
		this.reveals = reveals;
	}

	@Override
	protected Bounty complete(Bounty bounty) {
		return bounty.withChain(this.chains.findChain(bounty.language(), bounty.id()))
			.withReveals(this.reveals.findBountyReveals(bounty.id(), bounty.language()));
	}

}
