package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.FieldReveal;
import dev.onepieceapi.readertime.ReaderPosition;
import dev.onepieceapi.readertime.RevealedField;
import lombok.With;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * What the reader believes of a content at its first appearance and what is revealed
 * later (workflow 3.7.1); the main fields hold the current values. The values at N are
 * resolved by the library {@code one-piece-reader-time}, one field at a time.
 *
 * @param initialKnowledge null when no reveal names a field; a field not named is not
 * held
 * @param reveals in the version's order
 * @param <F> the revealable fields of the entity
 * @param <V> their values
 */
public record FieldReveals<F extends Enum<F>, V extends RevealedValues<F, V>>(V initialKnowledge,
		@With List<Reveal<F, V>> reveals) {

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

	/**
	 * The values as known at N: for each field a reveal names, the latest reveal reached,
	 * else the initial value, else unknown; the other fields keep their current value.
	 * @param unknown the values with no field held
	 */
	public V valuesAt(V current, V unknown, ReaderPosition position) {
		V known = current;
		for (F field : namedFields()) {
			V atN = revealedField(field, Function.identity(), current).valueAt(position).orElse(unknown);
			known = known.withField(field, atN);
		}
		return known;
	}

	/**
	 * The values of a field the reader has met by N (workflow 3.8, slugs): the initial
	 * one and those of the reveals reached, or only the current one when no reveal names
	 * the field. Missing values are left out.
	 * @param value the part of the values asked for
	 */
	public <T> Set<T> knownAt(F field, Function<V, T> value, T current, ReaderPosition position) {
		if (this.reveals.stream().noneMatch((reveal) -> reveal.names(field))) {
			return Stream.ofNullable(current).collect(Collectors.toUnmodifiableSet());
		}
		Stream<T> initial = Stream.ofNullable(initialValue(field, value));
		Stream<T> revealed = this.reveals.stream()
			.filter((reveal) -> reveal.names(field) && position.hasReached(reveal.revealed()))
			.map((reveal) -> reveal.values().holds(field) ? value.apply(reveal.values()) : current);
		return Stream.concat(initial, revealed).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
	}

	private List<F> namedFields() {
		return this.reveals.stream().flatMap((reveal) -> reveal.fields().stream()).distinct().toList();
	}

	private <T> RevealedField<T> revealedField(F field, Function<V, T> value, V current) {
		List<FieldReveal<T>> naming = this.reveals.stream()
			.filter((reveal) -> reveal.names(field))
			.map((reveal) -> reveal.of(field, value))
			.toList();
		return new RevealedField<>(value.apply(current), initialValue(field, value), naming);
	}

	private <T> T initialValue(F field, Function<V, T> value) {
		return (this.initialKnowledge != null && this.initialKnowledge.holds(field))
				? value.apply(this.initialKnowledge) : null;
	}

}
