package dev.onepieceapi.publicapi.domain;

/**
 * The values of the revealable fields of an entity (workflow 3.7.1): what the reader
 * believes at the first appearance, what a reveal teaches, or the current values.
 *
 * @param <F> the revealable fields of the entity
 * @param <V> the record itself
 */
public interface RevealedValues<F extends Enum<F>, V extends RevealedValues<F, V>> {

	/** Whether a value of the field is held here, rather than left to the current one. */
	boolean holds(F field);

	/** These values with the field's taken from {@code other}. */
	V withField(F field, V other);

}
