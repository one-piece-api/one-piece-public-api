package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.BountyField;
import dev.onepieceapi.publicapi.domain.BountyValues;
import dev.onepieceapi.publicapi.domain.CharacterField;
import dev.onepieceapi.publicapi.domain.CharacterStatus;
import dev.onepieceapi.publicapi.domain.CharacterValues;
import dev.onepieceapi.publicapi.domain.DevilFruitField;
import dev.onepieceapi.publicapi.domain.DevilFruitSubcategoryReference;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeReference;
import dev.onepieceapi.publicapi.domain.DevilFruitValues;
import dev.onepieceapi.publicapi.domain.FieldReveals;
import dev.onepieceapi.publicapi.domain.Reveal;
import dev.onepieceapi.publicapi.domain.RevealedValues;
import dev.onepieceapi.publicapi.persistence.row.BountyValuesRow;
import dev.onepieceapi.publicapi.persistence.row.CharacterValuesRow;
import dev.onepieceapi.publicapi.persistence.row.DevilFruitValuesRow;
import dev.onepieceapi.readertime.Point;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * From the rows of the initial knowledge and the reveals (workflow 3.7.1) to the domain.
 * A content with no reveal has no initial knowledge to show.
 */
@UtilityClass
public class RevealRowMapper {

	public FieldReveals<CharacterField, CharacterValues> toCharacterReveals(Optional<CharacterValuesRow> initial,
			List<CharacterValuesRow> reveals) {
		return toReveals(initial, reveals, RevealRowMapper::toValues, CharacterValuesRow::fields,
				CharacterValuesRow::title, (row) -> new Point(row.revealedChapter(), row.revealedEpisode()),
				CharacterField.class);
	}

	public FieldReveals<DevilFruitField, DevilFruitValues> toDevilFruitReveals(Optional<DevilFruitValuesRow> initial,
			List<DevilFruitValuesRow> reveals) {
		return toReveals(initial, reveals, RevealRowMapper::toValues, DevilFruitValuesRow::fields,
				DevilFruitValuesRow::title, (row) -> new Point(row.revealedChapter(), row.revealedEpisode()),
				DevilFruitField.class);
	}

	public FieldReveals<BountyField, BountyValues> toBountyReveals(Optional<BountyValuesRow> initial,
			List<BountyValuesRow> reveals) {
		return toReveals(initial, reveals, (row) -> new BountyValues(row.inactive()), BountyValuesRow::fields,
				BountyValuesRow::title, (row) -> new Point(row.revealedChapter(), row.revealedEpisode()),
				BountyField.class);
	}

	private CharacterValues toValues(CharacterValuesRow row) {
		CharacterStatus status = (row.status() == null) ? null : CharacterStatus.valueOf(row.status());
		return new CharacterValues(row.name(), row.romaji(), row.slug(), row.japaneseName(), status);
	}

	/** A type is held by its id: its name may be missing in the language. */
	private DevilFruitValues toValues(DevilFruitValuesRow row) {
		DevilFruitTypeReference type = (row.typeId() == null) ? null
				: new DevilFruitTypeReference(row.typeId(), row.typeSlug(), row.typeRomaji(), row.typeName());
		DevilFruitSubcategoryReference subcategory = (row.subcategoryId() == null) ? null
				: new DevilFruitSubcategoryReference(row.subcategoryId(), row.subcategoryName(),
						row.subcategoryDescription());
		return new DevilFruitValues(row.name(), row.romaji(), row.slug(), type, subcategory);
	}

	private <R, F extends Enum<F>, V extends RevealedValues<F, V>> FieldReveals<F, V> toReveals(Optional<R> initial,
			List<R> reveals, Function<R, V> values, Function<R, String> fields, Function<R, String> title,
			Function<R, Point> point, Class<F> fieldType) {
		if (reveals.isEmpty()) {
			return FieldReveals.none();
		}
		List<Reveal<F, V>> mapped = reveals.stream()
			.map((row) -> new Reveal<>(title.apply(row), point.apply(row), fieldsOf(fields.apply(row), fieldType),
					values.apply(row)))
			.toList();
		return new FieldReveals<>(initial.map(values).orElse(null), mapped);
	}

	private <F extends Enum<F>> Set<F> fieldsOf(String fields, Class<F> fieldType) {
		return Arrays.stream(fields.split(","))
			.map((field) -> Enum.valueOf(fieldType, field))
			.collect(Collectors.toUnmodifiableSet());
	}

}
