package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.DevilFruitType;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeLookup;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSearch;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSummary;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.persistence.PublishedDevilFruitTypeRepository;
import dev.onepieceapi.publicapi.service.exception.ContentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/** The Devil Fruit Types online, in the language of the path. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class DevilFruitTypeService {

	/** A UUID is recognised by its format; anything else is a slug (plan D4). */
	private static final Pattern UUID_FORMAT = Pattern
		.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

	private final LanguageService languages;

	private final PublishedDevilFruitTypeRepository repository;

	public Page<DevilFruitTypeSummary> search(String language, DevilFruitTypeSearch search) {
		this.languages.requireAvailable(language);
		return this.repository.search(language, search);
	}

	/**
	 * The type at an address, or where it moved: an old slug names the same content as
	 * its current one, which is where the caller is sent (plan D5).
	 */
	public DevilFruitTypeLookup find(String language, String idOrSlug) {
		this.languages.requireAvailable(language);
		DevilFruitType found = findOnline(language, idOrSlug).orElseThrow(() -> new ContentNotFoundException(idOrSlug));
		if (isId(idOrSlug) || idOrSlug.equals(found.slug())) {
			return new DevilFruitTypeLookup.Found(found);
		}
		// An old slug of a content whose romaji now leaves no slug has nowhere to point
		// to.
		if (found.slug() == null) {
			throw new ContentNotFoundException(idOrSlug);
		}
		return new DevilFruitTypeLookup.Moved(found.slug());
	}

	private Optional<DevilFruitType> findOnline(String language, String idOrSlug) {
		return isId(idOrSlug) ? this.repository.findById(language, UUID.fromString(idOrSlug))
				: this.repository.findBySlug(language, idOrSlug);
	}

	private static boolean isId(String idOrSlug) {
		return UUID_FORMAT.matcher(idOrSlug).matches();
	}

}
