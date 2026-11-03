package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.Point;

import java.util.Set;

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
public record Reveal<F extends Enum<F>, V extends RevealedValues<F, V>>(String title, Point revealed, Set<F> fields,
		V values) {

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

}
