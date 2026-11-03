package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.persistence.PublishedMeetingRepository;
import dev.onepieceapi.readertime.ReaderPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * What a reader at N sees, read once per request (reveals plan R7 D1): joins the caller's
 * transaction, so it describes the same snapshot as the data it filters.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ReaderSightService {

	private final PublishedMeetingRepository meetings;

	public ReaderSight at(ReaderPosition position, String language) {
		return ReaderSight.of(position, this.meetings.findAll(language));
	}

}
