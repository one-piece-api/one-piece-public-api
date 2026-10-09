package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.ContentLookup;
import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.domain.PublishedContent;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedView;
import dev.onepieceapi.publicapi.service.exception.ContentNotFoundException;
import dev.onepieceapi.publicapi.service.exception.LanguageNotAvailableException;
import org.junit.jupiter.api.Test;

import java.util.List;
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

	private final LanguageService languages = mock(LanguageService.class);

	private final PublishedContentRepository repository = mock(PublishedContentRepository.class);

	private final PublishedView<Note, String> view = PublishedView.<Note, String>builder().entityType("NOTE").build();

	private final PublishedContentService<Note, String> service = new PublishedContentService<>(this.languages,
			this.repository, this.view) {
	};

	@Test
	void findsByIdWithoutLookingAtTheSlug() {
		Note note = new Note(ID, null);
		when(this.repository.findById(this.view, "en", ID)).thenReturn(Optional.of(note));

		assertThat(this.service.find("en", ID.toString().toUpperCase())).isEqualTo(new ContentLookup.Found<>(note));
	}

	@Test
	void findsBySlugWhenItIsTheCurrentOne() {
		Note note = new Note(ID, "ichi");
		when(this.repository.findBySlug(this.view, "en", "ichi")).thenReturn(Optional.of(note));

		assertThat(this.service.find("en", "ichi")).isEqualTo(new ContentLookup.Found<>(note));
	}

	@Test
	void sendsAnOldSlugToTheCurrentOne() {
		when(this.repository.findBySlug(this.view, "en", "old")).thenReturn(Optional.of(new Note(ID, "ichi")));

		assertThat(this.service.find("en", "old")).isEqualTo(new ContentLookup.Moved<Note>("ichi"));
	}

	@Test
	void answersNotFoundForAnOldSlugOfAContentWithNoSlugNow() {
		when(this.repository.findBySlug(this.view, "en", "old")).thenReturn(Optional.of(new Note(ID, null)));

		assertThatThrownBy(() -> this.service.find("en", "old")).isInstanceOf(ContentNotFoundException.class);
	}

	@Test
	void answersNotFoundForWhatIsNotOnline() {
		when(this.repository.findById(this.view, "en", ID)).thenReturn(Optional.empty());
		when(this.repository.findBySlug(this.view, "en", "missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> this.service.find("en", ID.toString())).isInstanceOf(ContentNotFoundException.class);
		assertThatThrownBy(() -> this.service.find("en", "missing")).isInstanceOf(ContentNotFoundException.class);
	}

	@Test
	void checksTheLanguageBeforeReadingAnything() {
		doThrow(new LanguageNotAvailableException("xx")).when(this.languages).requireAvailable("xx");

		assertThatThrownBy(() -> this.service.find("xx", "ichi")).isInstanceOf(LanguageNotAvailableException.class);
		assertThatThrownBy(() -> this.service.search("xx",
				ContentSearch.builder().sort(ContentSort.DEFAULT).page(0).size(20).build()))
			.isInstanceOf(LanguageNotAvailableException.class);
		verifyNoInteractions(this.repository);
	}

	@Test
	void searchesThroughTheRepositoryWithItsView() {
		ContentSearch search = ContentSearch.builder().text("ich").sort(ContentSort.DEFAULT).page(0).size(20).build();
		Page<String> page = new Page<>(List.of("ichi"), 0, 20, 1);
		when(this.repository.search(this.view, "en", search)).thenReturn(page);

		assertThat(this.service.search("en", search)).isSameAs(page);
	}

	private record Note(UUID id, String slug) implements PublishedContent {

	}

}
