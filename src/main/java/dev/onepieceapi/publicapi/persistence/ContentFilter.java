package dev.onepieceapi.publicapi.persistence;

/**
 * A filter an entity declares on its list (plan D14): two fixed SQL conditions on its
 * view, one for an id and one for a slug (the same one for a value of a closed set). Each
 * takes its value as the bind parameter named {@code %s}, filled in by the repository;
 * nothing a caller sends ever becomes SQL (plan D3).
 *
 * @param byId the condition when the value is an id
 * @param bySlug the condition when the value is a slug, current or old
 */
public record ContentFilter(String byId, String bySlug) {

	/**
	 * A filter on a column holding the id of another content, matched by that content's
	 * id or by any slug it has had online.
	 */
	public static ContentFilter onContent(String column, String entityType) {
		return new ContentFilter(column + " = :%s", column + " = " + contentOfSlug(entityType));
	}

	/**
	 * A filter on a list of the content, kept in a child view: the contents whose child
	 * view has a row naming the other content, by its id or by any slug it has had online
	 * (the species of a character).
	 * @param childView the child view, such as {@code published.character_species}
	 * @param ownerColumn its column holding the id of the content listed
	 * @param column its column holding the id of the other content
	 * @param entityType the entity of the other content
	 */
	public static ContentFilter onChild(String childView, String ownerColumn, String column, String entityType) {
		String owners = "id IN (SELECT " + ownerColumn + " FROM " + childView + " WHERE " + column + " = ";
		return new ContentFilter(owners + ":%s)", owners + contentOfSlug(entityType) + ")");
	}

	/**
	 * A filter on a column holding one value of a closed set, such as the validity of a
	 * bounty: the controller reads the value as an enum, so only its names get here.
	 */
	public static ContentFilter onValue(String column) {
		String condition = column + " = :%s";
		return new ContentFilter(condition, condition);
	}

	/** The content of an entity that has, or had, the slug bound to {@code %s}. */
	private static String contentOfSlug(String entityType) {
		return "(SELECT content_id FROM published.content_slug WHERE entity_type = '" + entityType
				+ "' AND slug = :%s)";
	}

}
