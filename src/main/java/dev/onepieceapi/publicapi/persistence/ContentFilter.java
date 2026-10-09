package dev.onepieceapi.publicapi.persistence;

/**
 * A filter an entity declares on its list (plan D14): two fixed SQL conditions on its
 * view, one for an id and one for a slug. Each takes its value as the bind parameter
 * named {@code %s}, filled in by the repository; nothing a caller sends ever becomes SQL
 * (plan D3).
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
		return new ContentFilter(column + " = :%s", column + " = (SELECT content_id FROM published.content_slug"
				+ " WHERE entity_type = '" + entityType + "' AND slug = :%s)");
	}

}
