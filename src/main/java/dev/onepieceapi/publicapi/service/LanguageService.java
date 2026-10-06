package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Language;
import dev.onepieceapi.publicapi.persistence.PublishedLanguageRepository;
import dev.onepieceapi.publicapi.service.exception.LanguageNotAvailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** The public languages. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class LanguageService {

	private final PublishedLanguageRepository repository;

	public List<Language> list() {
		return this.repository.findAll();
	}

	/** Checked before any content is looked up (plan D6). */
	public void requireAvailable(String language) {
		if (!this.repository.exists(language)) {
			throw new LanguageNotAvailableException(language);
		}
	}

}
