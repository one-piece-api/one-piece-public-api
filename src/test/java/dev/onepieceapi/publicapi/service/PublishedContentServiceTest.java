package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ContentLookup;
import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.domain.PublishedContent;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.domain.SeenByReader;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedView;
import dev.onepieceapi.publicapi.service.exception.ContentNotFoundException;
import dev.onepieceapi.publicapi.service.exception.LanguageNotAvailableException;
import dev.onepieceapi.publicapi.service.exception.SortNotAvailableException;
import dev.onepieceapi.readertime.Point;
import dev.onepieceapi.readertime.ReaderPosition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The generic rules on an entity that exists only here, so a failure is not the Devil
 * Fruit Type's: the repository is a mock, the view only a name for it.
 */
class PublishedContentServiceTest {

	private static final UUID ID = UUID.fromString("0f6c3e5a-4b7d-4c1e-9a58-3d2b1c0e7f11");

	private static final ReaderPosition AT_CHAPTER_10 = ReaderPosition.atChapter(10);

	private static final Point EARLIER = Point.ofChapter(3);

	private static final Point LATER = Point.ofChapter(11);

	private final LanguageService languages = mock(LanguageService.class);

	private final PublishedContentRepository repository = mock(PublishedContentRepository.class);

	private final PublishedView<Note, Row> view = PublishedView.<Note, Row>builder().entityType("NOTE").build();

	private final ReaderSightService sights = mock(ReaderSightService.class);

	private final PublishedContentService<Note, Row> service = new PublishedContentService<>(this.languages,
			this.repository, this.sights, this.view) {
	};

	@Test
	void findsByIdWithoutLookingAtTheSlug() {
		Note note = new Note(ID, null);
		when(this.repository.findById(this.view, "en", ID)).thenReturn(Optional.of(note));

		assertThat(this.service.find("en", ID.toString().toUpperCase(), null))
			.isEqualTo(new ContentLookup.Found<>(note));
	}

	@Test
	void findsBySlugWhenItIsTheCurrentOne() {
		Note note = new Note(ID, "ichi");
		when(this.repository.findBySlug(this.view, "en", "ichi")).thenReturn(Optional.of(note));

		assertThat(this.service.find("en", "ichi", null)).isEqualTo(new ContentLookup.Found<>(note));
	}

	@Test
	void sendsAnOldSlugToTheCurrentOne() {
		when(this.repository.findBySlug(this.view, "en", "old")).thenReturn(Optional.of(new Note(ID, "ichi")));

		assertThat(this.service.find("en", "old", null)).isEqualTo(new ContentLookup.Moved<Note>("ichi"));
	}

	@Test
	void answersNotFoundForAnOldSlugOfAContentWithNoSlugNow() {
		when(this.repository.findBySlug(this.view, "en", "old")).thenReturn(Optional.of(new Note(ID, null)));

		assertThatThrownBy(() -> this.service.find("en", "old", null)).isInstanceOf(ContentNotFoundException.class);
	}

	@Test
	void answersNotFoundForWhatIsNotOnline() {
		when(this.repository.findById(this.view, "en", ID)).thenReturn(Optional.empty());
		when(this.repository.findBySlug(this.view, "en", "missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> this.service.find("en", ID.toString(), null))
			.isInstanceOf(ContentNotFoundException.class);
		assertThatThrownBy(() -> this.service.find("en", "missing", null)).isInstanceOf(ContentNotFoundException.class);
	}

	@Test
	void checksTheLanguageBeforeReadingAnything() {
		doThrow(new LanguageNotAvailableException("xx")).when(this.languages).requireAvailable("xx");

		assertThatThrownBy(() -> this.service.find("xx", "ichi", null))
			.isInstanceOf(LanguageNotAvailableException.class);
		assertThatThrownBy(() -> this.service.search("xx",
				ContentSearch.builder().sort(ContentSort.DEFAULT).page(0).size(20).build(), null))
			.isInstanceOf(LanguageNotAvailableException.class);
		verifyNoInteractions(this.repository);
	}

	@Test
	void searchesThroughTheRepositoryWithItsView() {
		ContentSearch search = ContentSearch.builder().text("ich").sort(ContentSort.DEFAULT).page(0).size(20).build();
		Page<Row> page = new Page<>(List.of(new Row(ID)), 0, 20, 1);
		when(this.repository.search(this.view, "en", search)).thenReturn(page);

		assertThat(this.service.search("en", search, null)).isSameAs(page);
	}

	@Test
	void refusesASortTheEntityDoesNotDeclareBeforeReadingAnything() {
		ContentSearch byAmount = ContentSearch.builder()
			.sort(new ContentSort(ContentSort.Field.AMOUNT, ContentSort.Direction.DESC))
			.page(0)
			.size(20)
			.build();

		assertThatThrownBy(() -> this.service.search("en", byAmount, null))
			.isInstanceOf(SortNotAvailableException.class)
			.hasMessage("This list cannot be sorted by amount");
		verifyNoInteractions(this.repository);
	}

	@Test
	void answersNotFoundForAContentTheReaderHasNotMetByIdOrSlug() {
		when(this.sights.at(AT_CHAPTER_10, "en")).thenReturn(ReaderSight.of(AT_CHAPTER_10, Map.of(ID, LATER)));
		when(this.repository.findById(this.view, "en", ID)).thenReturn(Optional.of(new Note(ID, "ichi")));
		when(this.repository.findBySlug(this.view, "en", "ichi")).thenReturn(Optional.of(new Note(ID, "ichi")));

		assertThatThrownBy(() -> this.service.find("en", ID.toString(), AT_CHAPTER_10))
			.isInstanceOf(ContentNotFoundException.class);
		assertThatThrownBy(() -> this.service.find("en", "ichi", AT_CHAPTER_10))
			.isInstanceOf(ContentNotFoundException.class);
	}

	@Test
	void findsAContentTheReaderHasMetAsTheReaderSeesIt() {
		Note note = new Note(ID, "ichi");
		when(this.sights.at(AT_CHAPTER_10, "en")).thenReturn(ReaderSight.of(AT_CHAPTER_10, Map.of(ID, EARLIER)));
		when(this.repository.findBySlug(this.view, "en", "ichi")).thenReturn(Optional.of(note));

		assertThat(this.service.find("en", "ichi", AT_CHAPTER_10))
			.isEqualTo(new ContentLookup.Found<>(note.atReader()));
	}

	@Test
	void leavesOutOfTheListWhatTheReaderHasNotMetThenPagesTheRest() {
		UUID other = UUID.randomUUID();
		UUID third = UUID.randomUUID();
		ContentSearch search = ContentSearch.builder().sort(ContentSort.DEFAULT).page(1).size(1).build();
		var meetings = Map.of(ID, EARLIER, other, LATER, third, EARLIER);
		when(this.sights.at(AT_CHAPTER_10, "en")).thenReturn(ReaderSight.of(AT_CHAPTER_10, meetings));
		when(this.repository.searchAll(this.view, "en", search))
			.thenReturn(List.of(new Row(ID), new Row(other), new Row(third)));

		assertThat(this.service.search("en", search, AT_CHAPTER_10))
			.isEqualTo(new Page<>(List.of(new Row(third)), 1, 1, 2));
	}

	private record Note(UUID id, String slug, boolean trimmed) implements PublishedContent, SeenByReader<Note> {

		Note(UUID id, String slug) {
			this(id, slug, false);
		}

		Note atReader() {
			return new Note(this.id, this.slug, true);
		}

		@Override
		public Note seenBy(ReaderSight sight) {
			return atReader();
		}

	}

	private record Row(UUID id) implements SeenByReader<Row> {

		@Override
		public Row seenBy(ReaderSight sight) {
			return this;
		}

	}

}
