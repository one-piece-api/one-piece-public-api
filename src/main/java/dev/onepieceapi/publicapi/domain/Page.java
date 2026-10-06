package dev.onepieceapi.publicapi.domain;

import java.util.List;

/** One page of a list, with how big the whole list is. */
public record Page<T>(List<T> content, int page, int size, long totalElements) {

	public int totalPages() {
		return (int) Math.ceilDiv(this.totalElements, this.size);
	}

}
