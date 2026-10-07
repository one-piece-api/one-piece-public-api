package dev.onepieceapi.publicapi.web.converter;

import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.domain.ContentSort.Direction;
import dev.onepieceapi.publicapi.domain.ContentSort.Field;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;
import java.util.Locale;

/**
 * {@code sort=field} or {@code sort=field,direction} to a {@link ContentSort}. A field or
 * direction outside the closed set fails the conversion, which the framework reports as a
 * {@code 400 VALIDATION_FAILED} on the parameter.
 */
public class ContentSortConverter implements Converter<String, ContentSort> {

	@Override
	public ContentSort convert(String source) {
		String[] parts = source.split(",", -1);
		if (parts.length > 2) {
			throw new IllegalArgumentException("sort must be field or field,direction");
		}
		Field field = Arrays.stream(Field.values())
			.filter(candidate -> candidate.getRequestName().equals(parts[0]))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("sort field not allowed: " + parts[0]));
		Direction direction = parts.length == 2 ? Direction.valueOf(parts[1].toUpperCase(Locale.ROOT)) : Direction.ASC;
		return new ContentSort(field, direction);
	}

}
