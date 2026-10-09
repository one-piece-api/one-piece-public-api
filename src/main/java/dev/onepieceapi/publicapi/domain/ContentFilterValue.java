package dev.onepieceapi.publicapi.domain;

/**
 * A filter of the list as asked for: one the entity declares, and the id or slug it
 * matches (plan D14).
 *
 * @param name the filter's name, as in the query string
 * @param idOrSlug what the filter matches
 */
public record ContentFilterValue(String name, String idOrSlug) {

}
