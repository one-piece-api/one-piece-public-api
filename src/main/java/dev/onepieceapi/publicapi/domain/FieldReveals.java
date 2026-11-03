package dev.onepieceapi.publicapi.domain;

import java.util.List;

/**
 * What the reader believes of a content at its first appearance and what is revealed
 * later (workflow 3.7.1); the main fields hold the current values.
 *
 * @param initialKnowledge null when no reveal names a field; a field not named is not
 * held
 * @param reveals in the version's order
 * @param <F> the revealable fields of the entity
 * @param <V> their values
 */
public record FieldReveals<F extends Enum<F>, V extends RevealedValues<F, V>>(V initialKnowledge,
		List<Reveal<F, V>> reveals) {

	public FieldReveals {
		reveals = List.copyOf(reveals);
	}

	/** A content with no reveal: nothing believed apart from its current values. */
	public static <F extends Enum<F>, V extends RevealedValues<F, V>> FieldReveals<F, V> none() {
		return new FieldReveals<>(null, List.of());
	}

	/** Every reveal of the current value carrying that value (reveals plan R7 S7). */
	public FieldReveals<F, V> filledWith(V current) {
		return new FieldReveals<>(this.initialKnowledge,
				this.reveals.stream().map((reveal) -> reveal.filledWith(current)).toList());
	}

}
