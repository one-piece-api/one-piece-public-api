package dev.onepieceapi.publicapi.domain;

import java.util.List;
import java.util.function.Function;

/** One page of a list, with how big the whole list is. */
public record Page<T>(List<T> content, int page, int size, long totalElements) {

	/** The page of a whole list already in memory; past the last, an empty one. */
	public static <T> Page<T> of(List<T> all, int page, int size) {
		int from = (int) Math.min((long) page * size, all.size());
		int to = (int) Math.min((long) from + size, all.size());
		return new Page<>(all.subList(from, to), page, size, all.size());
	}

	/** The same page with each row changed by {@code mapper}. */
	public <R> Page<R> map(Function<T, R> mapper) {
		return new Page<>(this.content.stream().map(mapper).toList(), this.page, this.size, this.totalElements);
	}

	public int totalPages() {
		return (int) Math.ceilDiv(this.totalElements, this.size);
	}

}
