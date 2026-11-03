package dev.onepieceapi.publicapi.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 * What the chapters (or the episodes) online of a parent amount to, derived from them
 * (story structure plan S7): never stored, never a list of them.
 *
 * @param count how many are online
 * @param firstNumber the lowest number; null when none
 * @param lastNumber the highest number; null when none
 * @param firstDate the earliest Japanese release (or broadcast) day among them; null when
 * none has one
 * @param lastDate the latest one; null when none has one
 */
public record StorySpan(int count, Integer firstNumber, Integer lastNumber, LocalDate firstDate, LocalDate lastDate) {

	/** No chapter or episode: the span of an axis the reader does not follow. */
	public static final StorySpan EMPTY = new StorySpan(0, null, null, null, null);

	/** The span of these days, as the views compute it from the rows online. */
	public static StorySpan of(Collection<StoryDay> days) {
		if (days.isEmpty()) {
			return EMPTY;
		}
		var numbers = days.stream().map(StoryDay::number).toList();
		var dates = days.stream().map(StoryDay::date).filter(Objects::nonNull).toList();
		return new StorySpan(days.size(), numbers.stream().min(Comparator.naturalOrder()).orElseThrow(),
				numbers.stream().max(Comparator.naturalOrder()).orElseThrow(),
				dates.stream().min(Comparator.naturalOrder()).orElse(null),
				dates.stream().max(Comparator.naturalOrder()).orElse(null));
	}

}
