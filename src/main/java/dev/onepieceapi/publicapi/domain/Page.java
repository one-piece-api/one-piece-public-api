package dev.onepieceapi.publicapi.domain;

import java.util.List;

/** One page of a list, with how big the whole list is. */
public record Page<T>(List<T> content, int page, int size, long totalElements) {

	/** The page of a whole list already in memory; past the last, an empty one. */
	public static <T> Page<T> of(List<T> all, int page, int size) {
		int from = (int) Math.min((long) page * size, all.size());
		int to = (int) Math.min((long) from + size, all.size());
		return new Page<>(all.subList(from, to), page, size, all.size());
	}

	public int totalPages() {
		return (int) Math.ceilDiv(this.totalElements, this.size);
	}

}
