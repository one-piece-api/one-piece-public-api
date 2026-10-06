package dev.onepieceapi.publicapi.domain;

/**
 * One page of the list: which, in which order and how many.
 *
 * @param text what the name or the romaji contains; null for no filter
 * @param page 0-based
 */
public record DevilFruitTypeSearch(String text, DevilFruitTypeSort sort, int page, int size) {

	/** Whether the list is narrowed by a text; without one, nothing is compared. */
	public boolean filtered() {
		return this.text != null;
	}

	public long offset() {
		return (long) this.page * this.size;
	}

}
