package dev.onepieceapi.publicapi.web.converter;

import dev.onepieceapi.publicapi.domain.DevilFruitTypeSort;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSort.Direction;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSort.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DevilFruitTypeSortConverterTest {

	private final DevilFruitTypeSortConverter converter = new DevilFruitTypeSortConverter();

	@Test
	void readsAFieldAloneAsAscending() {
		assertThat(this.converter.convert("romaji")).isEqualTo(new DevilFruitTypeSort(Field.ROMAJI, Direction.ASC));
	}

	@Test
	void readsAFieldWithItsDirectionInAnyCase() {
		assertThat(this.converter.convert("publishedAt,DESC"))
			.isEqualTo(new DevilFruitTypeSort(Field.PUBLISHED_AT, Direction.DESC));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "password", "NAME", "name,", "name,sideways", "name,asc,asc" })
	void refusesAnythingOutsideTheClosedSet(String source) {
		assertThatIllegalArgumentException().isThrownBy(() -> this.converter.convert(source));
	}

}
