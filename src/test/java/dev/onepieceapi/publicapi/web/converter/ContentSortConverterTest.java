package dev.onepieceapi.publicapi.web.converter;

import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.domain.ContentSort.Direction;
import dev.onepieceapi.publicapi.domain.ContentSort.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ContentSortConverterTest {

	private final ContentSortConverter converter = new ContentSortConverter();

	@Test
	void readsAFieldAloneAsAscending() {
		assertThat(this.converter.convert("romaji")).isEqualTo(new ContentSort(Field.ROMAJI, Direction.ASC));
	}

	@Test
	void readsAFieldWithItsDirectionInAnyCase() {
		assertThat(this.converter.convert("publishedAt,DESC"))
			.isEqualTo(new ContentSort(Field.PUBLISHED_AT, Direction.DESC));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "password", "NAME", "name,", "name,sideways", "name,asc,asc" })
	void refusesAnythingOutsideTheClosedSet(String source) {
		assertThatIllegalArgumentException().isThrownBy(() -> this.converter.convert(source));
	}

}
