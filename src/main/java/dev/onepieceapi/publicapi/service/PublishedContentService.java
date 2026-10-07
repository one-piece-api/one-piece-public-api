package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ContentLookup;
import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.domain.PublishedContent;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedView;
import dev.onepieceapi.publicapi.service.exception.ContentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The contents of one entity online, in the language of the path. What changes from an
 * entity to another is the {@link PublishedView} it is built with and its two types; the
 * rules are these (template: a thin subclass per entity binds them).
 *
 * @param <D> the detail
 * @param <S> the list row
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
public abstract class PublishedContentService<D extends PublishedContent, S> {

	/** A UUID is recognised by its format; anything else is a slug (plan D4). */
	private static final Pattern UUID_FORMAT = Pattern
		.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

	private final LanguageService languages;

	private final PublishedContentRepository repository;

	private final PublishedView<D, S> view;

	public Page<S> search(String language, ContentSearch search) {
		this.languages.requireAvailable(language);
		return this.repository.search(this.view, language, search);
	}

	/**
	 * The content at an address, or where it moved: an old slug names the same content as
	 * its current one, which is where the caller is sent (plan D5).
	 */
	public ContentLookup<D> find(String language, String idOrSlug) {
		this.languages.requireAvailable(language);
		D found = findOnline(language, idOrSlug).orElseThrow(() -> new ContentNotFoundException(idOrSlug));
		if (isId(idOrSlug) || idOrSlug.equals(found.slug())) {
			return new ContentLookup.Found<>(found);
		}
		// An old slug of a content whose romaji now leaves no slug has nowhere to point
		// to.
		if (found.slug() == null) {
			throw new ContentNotFoundException(idOrSlug);
		}
		return new ContentLookup.Moved<>(found.slug());
	}

	private Optional<D> findOnline(String language, String idOrSlug) {
		return isId(idOrSlug) ? this.repository.findById(this.view, language, UUID.fromString(idOrSlug))
				: this.repository.findBySlug(this.view, language, idOrSlug);
	}

	private static boolean isId(String idOrSlug) {
		return UUID_FORMAT.matcher(idOrSlug).matches();
	}

}
