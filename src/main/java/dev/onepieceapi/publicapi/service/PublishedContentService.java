package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ContentAddress;
import dev.onepieceapi.publicapi.domain.ContentLookup;
import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.domain.PublishedContent;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.domain.SeenByReader;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedView;
import dev.onepieceapi.publicapi.service.exception.ContentNotFoundException;
import dev.onepieceapi.publicapi.service.exception.SortNotAvailableException;
import dev.onepieceapi.readertime.ReaderPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * The contents of one entity online, in the language of the path. What changes from an
 * entity to another is the {@link PublishedView} it is built with and its two types; the
 * rules are these (template: a thin subclass per entity binds them).
 * <p>
 * Two paths (reveals plan R7 D1): without a reader position, the SQL of the view pages
 * the list; with one, every row of the search is read, those the reader has not met are
 * left out, and the rest is paged in memory, each content as the reader sees it.
 *
 * @param <D> the detail
 * @param <S> the list row
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
public abstract class PublishedContentService<D extends PublishedContent & SeenByReader<D>, S extends SeenByReader<S>> {

	private final LanguageService languages;

	private final PublishedContentRepository repository;

	private final ReaderSightService sights;

	private final PublishedView<D, S> view;

	/**
	 * @param position where the reader is; null for no spoiler filter
	 */
	public Page<S> search(String language, ContentSearch search, ReaderPosition position) {
		this.languages.requireAvailable(language);
		if (!this.view.sortable(search.sort().field())) {
			throw new SortNotAvailableException(search.sort().field());
		}
		if (position == null) {
			return this.repository.search(this.view, language, search);
		}
		ReaderSight sight = this.sights.at(position, language);
		return Page.of(sight.shown(this.repository.searchAll(this.view, language, search)), search.page(),
				search.size());
	}

	/**
	 * The content at an address, or where it moved: an old slug names the same content as
	 * its current one, which is where the caller is sent (plan D5). A content the reader
	 * has not met is not found, never forbidden: that would tell it exists (workflow
	 * 3.8).
	 * @param position where the reader is; null for no spoiler filter
	 */
	public ContentLookup<D> find(String language, String idOrSlug, ReaderPosition position) {
		this.languages.requireAvailable(language);
		ReaderSight sight = (position != null) ? this.sights.at(position, language) : null;
		D found = findOnline(language, idOrSlug).filter((content) -> sight == null || sight.sees(content.id()))
			.orElseThrow(() -> new ContentNotFoundException(idOrSlug));
		if (ContentAddress.isId(idOrSlug) || idOrSlug.equals(found.slug())) {
			return new ContentLookup.Found<>((sight == null) ? complete(found) : completeFor(found, sight));
		}
		// An old slug of a content whose romaji now leaves no slug has nowhere to point
		// to.
		if (found.slug() == null) {
			throw new ContentNotFoundException(idOrSlug);
		}
		return new ContentLookup.Moved<>(found.slug());
	}

	/**
	 * What an entity adds to its detail from other views, such as the contents it holds
	 * or its release days; the content itself when it adds nothing (hook of the
	 * template).
	 */
	protected D complete(D content) {
		return content;
	}

	/**
	 * The detail as the reader sees it: completed, then its links to contents not met yet
	 * removed. An entity overrides it for what else depends on the position, such as its
	 * spans or counts (hook of the template).
	 */
	protected D completeFor(D content, ReaderSight sight) {
		return complete(content).seenBy(sight);
	}

	private Optional<D> findOnline(String language, String idOrSlug) {
		return ContentAddress.isId(idOrSlug) ? this.repository.findById(this.view, language, UUID.fromString(idOrSlug))
				: this.repository.findBySlug(this.view, language, idOrSlug);
	}

}
