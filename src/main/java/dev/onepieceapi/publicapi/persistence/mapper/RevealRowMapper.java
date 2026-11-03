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
import dev.onepieceapi.publicapi.persistence.row.RevealRow;
import dev.onepieceapi.readertime.Point;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * From the rows of the initial knowledge and the reveals (workflow 3.7.1) to the domain,
 * by content: the reveals of each in the rows' order. A content with no reveal has no
 * initial knowledge to show, and is left out.
 */
@UtilityClass
public class RevealRowMapper {

	public Map<UUID, FieldReveals<CharacterField, CharacterValues>> toCharacterReveals(
			List<CharacterValuesRow> initials, List<CharacterValuesRow> reveals) {
		return group(initials, reveals, RevealRowMapper::toValues, CharacterField.class);
	}

	public Map<UUID, FieldReveals<DevilFruitField, DevilFruitValues>> toDevilFruitReveals(
			List<DevilFruitValuesRow> initials, List<DevilFruitValuesRow> reveals) {
		return group(initials, reveals, RevealRowMapper::toValues, DevilFruitField.class);
	}

	public Map<UUID, FieldReveals<BountyField, BountyValues>> toBountyReveals(List<BountyValuesRow> initials,
			List<BountyValuesRow> reveals) {
		return group(initials, reveals, (row) -> new BountyValues(row.inactive()), BountyField.class);
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

	private <R extends RevealRow, F extends Enum<F>, V extends RevealedValues<F, V>> Map<UUID, FieldReveals<F, V>> group(
			List<R> initials, List<R> reveals, Function<R, V> values, Class<F> fieldType) {
		Function<R, Reveal<F, V>> toReveal = (row) -> new Reveal<>(row.title(),
				new Point(row.revealedChapter(), row.revealedEpisode()), fieldsOf(row.fields(), fieldType),
				values.apply(row));
		Map<UUID, V> initialById = initials.stream().collect(Collectors.toMap(RevealRow::contentId, values));
		Map<UUID, List<Reveal<F, V>>> revealsById = reveals.stream()
			.collect(Collectors.groupingBy(RevealRow::contentId, Collectors.mapping(toReveal, Collectors.toList())));
		Map<UUID, FieldReveals<F, V>> byId = new HashMap<>();
		revealsById.forEach((id, named) -> byId.put(id, new FieldReveals<>(initialById.get(id), named)));
		return byId;
	}

	private <F extends Enum<F>> Set<F> fieldsOf(String fields, Class<F> fieldType) {
		return Arrays.stream(fields.split(","))
			.map((field) -> Enum.valueOf(fieldType, field))
			.collect(Collectors.toUnmodifiableSet());
	}

}
