package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.FieldReveal;
import dev.onepieceapi.readertime.Point;
import lombok.With;

import java.util.Set;
import java.util.function.Function;

/**
 * A point where the reader learns some fields of a content (workflow 3.7.1).
 *
 * @param title in the reader's language; null when none is written in it
 * @param revealed where the reader learns
 * @param fields the fields learned there
 * @param values the value learned for each field named; a field named without one reveals
 * the current value
 * @param <F> the revealable fields of the entity
 * @param <V> their values
 */
public record Reveal<F extends Enum<F>, V extends RevealedValues<F, V>>(String title, @With Point revealed,
		Set<F> fields, V values) {

	public Reveal {
		fields = Set.copyOf(fields);
	}

	/**
	 * The same reveal with the current value written for each field it reveals as such: a
	 * client reads the value, never the rule (reveals plan R7 S7).
	 */
	public Reveal<F, V> filledWith(V current) {
		V filled = this.values;
		for (F field : this.fields) {
			if (!filled.holds(field)) {
				filled = filled.withField(field, current);
			}
		}
		return new Reveal<>(this.title, this.revealed, this.fields, filled);
	}

	/** Whether the reader learns the field here. */
	public boolean names(F field) {
		return this.fields.contains(field);
	}

	/**
	 * What the reader learns here of a field it names, as the library reads it: the value
	 * learned, or the current one.
	 * @param value the part of the values the field is about
	 */
	public <T> FieldReveal<T> of(F field, Function<V, T> value) {
		return this.values.holds(field) ? FieldReveal.learned(this.revealed, value.apply(this.values))
				: FieldReveal.ofCurrentValue(this.revealed);
	}

}
