package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * The revealable values of a Devil Fruit, read from
 * {@code published.devil_fruit_initial_knowledge} or {@code published.devil_fruit_reveal}
 * with the name, the type and the subcategory in the language of the path; a value not
 * held is null.
 *
 * @param contentId the content whose values these are
 * @param fields the fields a reveal names, comma separated; null for the initial
 * knowledge
 * @param title the reveal's title; null for the initial knowledge
 * @param revealedChapter where a reveal is learned; null for the initial knowledge
 */
public record DevilFruitValuesRow(UUID contentId, String fields, String title, String name, String romaji, String slug,
		UUID typeId, String typeSlug, String typeRomaji, String typeName, UUID subcategoryId, String subcategoryName,
		String subcategoryDescription, Integer revealedChapter, Integer revealedEpisode) implements RevealRow {

}
