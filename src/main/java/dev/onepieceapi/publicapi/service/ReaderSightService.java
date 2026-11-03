package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ReaderKnowledge;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.persistence.PublishedBountyChainRepository;
import dev.onepieceapi.publicapi.persistence.PublishedMeetingRepository;
import dev.onepieceapi.publicapi.persistence.PublishedRevealRepository;
import dev.onepieceapi.readertime.ReaderPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * What a reader at N sees, read once per request (reveals plan R7 D1, S10): where the
 * reader meets each content and what the reader may know of its revealable fields. Joins
 * the caller's transaction, so it describes the same snapshot as the data it filters.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ReaderSightService {

	private final PublishedMeetingRepository meetings;

	private final PublishedRevealRepository reveals;

	private final PublishedBountyChainRepository bounties;

	public ReaderSight at(ReaderPosition position, String language) {
		ReaderKnowledge knowledge = ReaderKnowledge.builder()
			.characters(this.reveals.findAllCharacterReveals(language))
			.devilFruits(this.reveals.findAllDevilFruitReveals(language))
			.bounties(this.reveals.findAllBountyReveals(language))
			.successors(this.bounties.findSuccessors(language))
			.build();
		return ReaderSight.of(position, this.meetings.findAll(language), knowledge);
	}

}
