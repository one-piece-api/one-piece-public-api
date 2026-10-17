package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.util.List;

/**
 * One page of the list: which, in which order and how many.
 *
 * @param text what the name or the romaji contains; null for no filter
 * @param filters the filters of the entity asked for (plan D14); none when null
 * @param page 0-based
 * @param asOfChapter the spoiler filter; none when null
 */
@Builder
public record ContentSearch(String text, List<ContentFilterValue> filters, ContentSort sort, int page, int size,
		@With AsOfChapter asOfChapter) {

	public ContentSearch {
		filters = filters == null ? List.of() : List.copyOf(filters);
		asOfChapter = asOfChapter == null ? AsOfChapter.NONE : asOfChapter;
	}

	/** Whether the list is narrowed by a text; without one, nothing is compared. */
	public boolean filtered() {
		return this.text != null;
	}

	public long offset() {
		return (long) this.page * this.size;
	}

}
