package dev.onepieceapi.publicapi.persistence;

/**
 * Where the days an entity was released (or broadcast) per country are read: a view of
 * the {@code published} schema and its columns. Fixed SQL fragments, as in
 * {@link PublishedView} (plan D3).
 *
 * @param view the view, with its schema
 * @param idColumn the column holding the id of the content
 * @param dateColumn the column holding the day
 */
public record PublishedReleases(String view, String idColumn, String dateColumn) {

}
