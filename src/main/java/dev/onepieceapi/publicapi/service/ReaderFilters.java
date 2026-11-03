package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.CharacterEntries;
import dev.onepieceapi.publicapi.domain.ContentFilterValue;
import dev.onepieceapi.publicapi.domain.ReaderSight;
import dev.onepieceapi.publicapi.persistence.ContentFilter;

import java.util.List;

/**
 * The filters of a list as the reader at N asks for them, their values resolved (reveals
 * plan R7 S11): those on a value that never changes, which the database tests, and those
 * on a value the reader learns by degrees, which are tested in memory on the row at N.
 *
 * @param fixed the filters for the database, a content named by its id
 * @param atN the filters tested in memory
 * @param <S> the list row
 */
record ReaderFilters<S>(List<ContentFilterValue> fixed, List<AtN<S>> atN) {

	/**
	 * A filter tested at N with the value it was asked for.
	 *
	 * @param value the id of the content the filter names, or the value of a closed set
	 */
	record AtN<S>(ContentFilter<S> filter, String value) {

		boolean matches(S row, ReaderSight sight, CharacterEntries entries) {
			return this.filter.atN().matches(row, this.value, sight, entries);
		}

	}

}
