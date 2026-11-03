package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.CharacterEntries;
import dev.onepieceapi.publicapi.domain.ReaderSight;

/**
 * A filter an entity declares on its list (plan D14). Without a spoiler filter it is two
 * fixed SQL conditions on its view, one for an id and one for a slug (the same one for a
 * value of a closed set); each takes its value as the bind parameter named {@code %s},
 * filled in by the repository. Nothing a caller sends ever becomes SQL (plan D3).
 * <p>
 * With a spoiler filter (reveals plan R7 S11) a filter is of one of two families. A
 * <em>fixed</em> one, {@code atN} null, tests a value that never changes (the arc of a
 * chapter): it stays SQL, with the id its value resolves to. One <em>at N</em> tests the
 * row as the reader at N sees it, in memory, next to the SQL one.
 *
 * @param byId the condition when the value is an id
 * @param bySlug the condition when the value is a slug, current or old
 * @param entityType the entity the value names, when it names a content; null for a value
 * of a closed set. The reader must have met that content, else nothing matches
 * @param atN the test of the row at N; null for a fixed filter
 * @param readsEntries whether {@code atN} tests the lists of a character
 * @param <S> the list row
 */
public record ContentFilter<S>(String byId, String bySlug, String entityType, AtN<S> atN, boolean readsEntries) {

	/**
	 * The test of a row as the reader at N sees it (row already seen at N).
	 */
	@FunctionalInterface
	public interface AtN<S> {

		/**
		 * @param value the id the filter's content resolved to, or the value of the
		 * closed set
		 * @param entries the lists of the characters, when the filter reads them
		 */
		boolean matches(S row, String value, ReaderSight sight, CharacterEntries entries);

	}

	/**
	 * A filter on a column holding the id of another content, matched by that content's
	 * id or by any slug it has had online; fixed.
	 */
	public static <S> ContentFilter<S> onContent(String column, String entityType) {
		return onContent(column, entityType, null);
	}

	/** The same, tested at N in memory. */
	public static <S> ContentFilter<S> onContent(String column, String entityType, AtN<S> atN) {
		return new ContentFilter<>(column + " = :%s", column + " = " + contentOfSlug(entityType), entityType, atN,
				false);
	}

	/**
	 * A filter on a list of the content, kept in a child view: the contents whose child
	 * view has a row naming the other content, by its id or by any slug it has had online
	 * (the species of a character).
	 * @param childView the child view, such as {@code published.character_species}
	 * @param ownerColumn its column holding the id of the content listed
	 * @param column its column holding the id of the other content
	 * @param entityType the entity of the other content
	 * @param atN the test of the lists at N
	 */
	public static <S> ContentFilter<S> onChild(String childView, String ownerColumn, String column, String entityType,
			AtN<S> atN) {
		return onChild(childView, ownerColumn, column, entityType, "TRUE", atN);
	}

	/**
	 * The same, for the rows of the child view that also meet a fixed condition, such as
	 * {@code NOT former} for the affiliations a character belongs to now.
	 */
	public static <S> ContentFilter<S> onChild(String childView, String ownerColumn, String column, String entityType,
			String andCondition, AtN<S> atN) {
		String owners = "id IN (SELECT " + ownerColumn + " FROM " + childView + " WHERE " + andCondition + " AND "
				+ column + " = ";
		return new ContentFilter<>(owners + ":%s)", owners + contentOfSlug(entityType) + ")", entityType, atN, true);
	}

	/**
	 * A filter on a column holding one value of a closed set, such as the validity of a
	 * bounty: the controller reads the value as an enum, so only its names get here.
	 * Fixed.
	 */
	public static <S> ContentFilter<S> onValue(String column) {
		return onValue(column, null);
	}

	/** The same, tested at N in memory. */
	public static <S> ContentFilter<S> onValue(String column, AtN<S> atN) {
		String condition = column + " = :%s";
		return new ContentFilter<>(condition, condition, null, atN, false);
	}

	/** Whether the filter is tested at N in memory. */
	public boolean testedAtN() {
		return this.atN != null;
	}

	/** The content of an entity that has, or had, the slug bound to {@code %s}. */
	private static String contentOfSlug(String entityType) {
		return "(SELECT content_id FROM published.content_slug WHERE entity_type = '" + entityType
				+ "' AND slug = :%s)";
	}

}
