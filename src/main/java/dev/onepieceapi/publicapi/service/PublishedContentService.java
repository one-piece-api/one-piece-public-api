package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.CharacterEntries;
import dev.onepieceapi.publicapi.domain.ContentAddress;
import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.ContentLookup;
import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.domain.LongTexts;
import dev.onepieceapi.publicapi.domain.NameOrder;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.domain.PublishedContent;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.domain.SeenByReader;
import dev.onepieceapi.publicapi.persistence.ContentFilter;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedView;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import dev.onepieceapi.publicapi.service.exception.ContentNotFoundException;
import dev.onepieceapi.publicapi.service.exception.SortNotAvailableException;
import dev.onepieceapi.readertime.ReaderPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

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
			return completeRows(this.repository.search(this.view, language, search), language);
		}
		ReaderSight sight = this.sights.at(position, language);
		return completeRowsFor(Page.of(searchAt(language, search, sight), search.page(), search.size()), language,
				sight);
	}

	/**
	 * Every row of the search the reader has met, as the reader sees it, in the order
	 * asked for (reveals plan R7 S11, S12). The filters on a content are resolved first,
	 * through the reader's sight: a content the reader has not met matches nothing. A
	 * filter on a value that never changes stays SQL; one on a value the reader learns by
	 * degrees is tested in memory, on the row at N.
	 */
	private List<S> searchAt(String language, ContentSearch search, ReaderSight sight) {
		Optional<ReaderFilters<S>> filters = resolveFilters(language, search, sight);
		if (filters.isEmpty()) {
			return List.of();
		}
		ContentSearch inDatabase = search.toBuilder()
			.filters(filters.get().fixed())
			.text(this.view.namedByDegrees() ? null : search.text())
			.build();
		List<S> seen = matching(sight.shown(this.repository.searchAll(this.view, language, inDatabase)),
				filters.get().atN(), language, sight);
		return this.view.namedByDegrees() ? orderedByNameAtN(containing(seen, search.text()), search.sort(), language)
				: seen;
	}

	/**
	 * The filters asked for, resolved; empty when one names a content the reader has not
	 * met.
	 */
	private Optional<ReaderFilters<S>> resolveFilters(String language, ContentSearch search, ReaderSight sight) {
		List<ContentFilterValue> fixed = new ArrayList<>();
		List<ReaderFilters.AtN<S>> atN = new ArrayList<>();
		for (ContentFilterValue asked : search.filters()) {
			ContentFilter<S> declared = this.view.filters().get(asked.name());
			Optional<String> value = resolvedValue(declared, asked.idOrSlug(), language, sight);
			if (value.isEmpty()) {
				return Optional.empty();
			}
			if (declared.testedAtN()) {
				atN.add(new ReaderFilters.AtN<>(declared, value.get()));
			}
			else {
				fixed.add(new ContentFilterValue(asked.name(), value.get()));
			}
		}
		return Optional.of(new ReaderFilters<>(fixed, atN));
	}

	/**
	 * What the filter tests: the id of the content its value names, when the reader has
	 * met it and, for a slug, may know it by then (plan D13); the value itself for a
	 * value of a closed set.
	 */
	private Optional<String> resolvedValue(ContentFilter<S> declared, String value, String language,
			ReaderSight sight) {
		if (declared.entityType() == null) {
			return Optional.of(value);
		}
		boolean byId = ContentAddress.isId(value);
		return this.repository.findIdentity(PublishedViews.ofEntity(declared.entityType()), language, value)
			.filter((content) -> sight.sees(content.id()))
			.filter((content) -> byId || sight.knowsSlug(content.id(), value, content.slug()))
			.map((content) -> content.id().toString());
	}

	private List<S> matching(List<S> seen, List<ReaderFilters.AtN<S>> tests, String language, ReaderSight sight) {
		if (tests.isEmpty()) {
			return seen;
		}
		CharacterEntries entries = tests.stream().anyMatch((test) -> test.filter().readsEntries()) ? entriesOf(language)
				: CharacterEntries.NONE;
		return seen.stream()
			.filter((row) -> tests.stream().allMatch((test) -> test.matches(row, sight, entries)))
			.toList();
	}

	/** Those whose name or romaji at N contains the text, case apart; all for no text. */
	private List<S> containing(List<S> rows, String text) {
		if (text == null) {
			return rows;
		}
		String wanted = text.toLowerCase(Locale.ROOT);
		var revealed = this.view.revealedName();
		return rows.stream()
			.filter((row) -> contains(revealed.name().apply(row), wanted)
					|| contains(revealed.romaji().apply(row), wanted))
			.toList();
	}

	private static boolean contains(String value, String wanted) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(wanted);
	}

	/**
	 * By the name or the romaji at N, in the order of the path language, then by id as
	 * the database does; a sort on a field the reader does not learn by degrees keeps the
	 * order of the database.
	 */
	private List<S> orderedByNameAtN(List<S> rows, ContentSort sort, String language) {
		var revealed = this.view.revealedName();
		Function<S, String> key = switch (sort.field()) {
			case NAME -> revealed.name();
			case ROMAJI, NUMBER -> revealed.romaji();
			case PUBLISHED_AT, AMOUNT -> null;
		};
		if (key == null) {
			return rows;
		}
		Comparator<S> byText = Comparator.comparing(key, NameOrder.in(language));
		Comparator<S> order = ((sort.direction() == ContentSort.Direction.DESC) ? byText.reversed() : byText)
			.thenComparing((row) -> row.id());
		return rows.stream().sorted(order).toList();
	}

	/**
	 * The content at an address, or where it moved: an old slug names the same content as
	 * its current one, which is where the caller is sent (plan D5). A content the reader
	 * has not met is not found, never forbidden: that would tell it exists (workflow
	 * 3.8).
	 * <p>
	 * With a reader position the slug at N is the canonical one (plan D13): another slug
	 * the reader may know by then moves there, a slug the reader cannot know yet is not
	 * found.
	 * @param position where the reader is; null for no spoiler filter
	 */
	public ContentLookup<D> find(String language, String idOrSlug, ReaderPosition position) {
		this.languages.requireAvailable(language);
		ReaderSight sight = (position != null) ? this.sights.at(position, language) : null;
		D found = findOnline(language, idOrSlug).filter((content) -> sight == null || sight.sees(content.id()))
			.orElseThrow(() -> new ContentNotFoundException(idOrSlug));
		String canonical = (sight == null) ? found.slug() : sight.slugAt(found.id(), found.slug());
		if (ContentAddress.isId(idOrSlug) || idOrSlug.equals(canonical)) {
			D described = withParagraphs(found, language, sight);
			return new ContentLookup.Found<>((sight == null) ? complete(described) : completeFor(described, sight));
		}
		// An old slug of a content whose romaji leaves no slug has nowhere to point to.
		if (canonical == null || (sight != null && !sight.knowsSlug(found.id(), idOrSlug, found.slug()))) {
			throw new ContentNotFoundException(idOrSlug);
		}
		return new ContentLookup.Moved<>(canonical);
	}

	/**
	 * The lists of the characters that a filter at N tests, read once for the request
	 * (hook of the template); none for an entity whose filters test no list.
	 */
	protected CharacterEntries entriesOf(String language) {
		return CharacterEntries.NONE;
	}

	/**
	 * What an entity adds to the rows of a page from other views, such as a portrait
	 * derived from a gallery; the page itself when it adds nothing (hook of the
	 * template).
	 */
	protected Page<S> completeRows(Page<S> page, String language) {
		return page;
	}

	/**
	 * The rows of a page as the reader sees them, already seen at N: what the entity adds
	 * from other views, at N too. An entity overrides it when what it adds depends on the
	 * position, such as a portrait (hook of the template).
	 */
	protected Page<S> completeRowsFor(Page<S> page, String language, ReaderSight sight) {
		return completeRows(page, language);
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

	/**
	 * Its long texts, for an entity that has them (workflow 3.7.3): the paragraphs
	 * without an end, or those the reader reads at N.
	 * @param sight null for no spoiler filter
	 */
	private D withParagraphs(D content, String language, ReaderSight sight) {
		if (!this.view.paragraphed()) {
			return content;
		}
		LongTexts texts = this.repository.findParagraphs(content.id(), language);
		return this.view.paragraphs().apply(content, (sight == null) ? texts.current() : texts.seenBy(sight));
	}

	private Optional<D> findOnline(String language, String idOrSlug) {
		return ContentAddress.isId(idOrSlug) ? this.repository.findById(this.view, language, UUID.fromString(idOrSlug))
				: this.repository.findBySlug(this.view, language, idOrSlug);
	}

}
