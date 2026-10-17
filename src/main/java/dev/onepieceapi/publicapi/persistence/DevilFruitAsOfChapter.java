package dev.onepieceapi.publicapi.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Devil Fruits online as the reader knows them by chapter {@code :asOfChapter} (plan
 * of the timeline, T13, TL6): a fixed SQL source with the columns of
 * {@code published.devil_fruit}, read in its place when the spoiler filter applies.
 * <p>
 * Per field: the value of the latest reveal up to that chapter naming it; else, if a
 * later reveal names it, the initial knowledge (null when unknown); else the truth. Then
 * what the reader has not met yet is null: a type not first appeared by then, with its
 * subcategory; a subcategory first appeared later (with none, it is met with its type);
 * the slug, when the romaji known is not the true one it comes from.
 * <p>
 * Each published view is read once per fruit (one join for the reveals, one for the type
 * met, one for the subcategory met): inlining the views once per column made the query
 * plan explode past the reader role's statement timeout.
 */
final class DevilFruitAsOfChapter {

	/** The chapters online up to the one read, by id (as {@code FIRST_APPEARED}). */
	private static final String CHAPTERS_READ = "(SELECT id FROM published.chapter WHERE number <= :asOfChapter)";

	/** The fields a reveal names, each with the columns it resolves. */
	private static final List<RevealedField> FIELDS = List.of(new RevealedField("NAME", "name"),
			new RevealedField("ROMAJI", "romaji"),
			new RevealedField("TYPE", "type_id", "type_slug", "type_romaji", "type_name", "subcategory_id",
					"subcategory_name", "subcategory_description"),
			new RevealedField("DESCRIPTION", "description"), new RevealedField("ADVANTAGES", "advantages"),
			new RevealedField("DISADVANTAGES", "disadvantages"));

	/** Met by then: the type known, first appeared up to the chapter. */
	private static final String TYPE_MET = "LEFT JOIN LATERAL (SELECT true AS met FROM published.devil_fruit_type ty"
			+ " WHERE ty.id = r.type_id AND ty.first_appearance_chapter_id IN " + CHAPTERS_READ
			+ " LIMIT 1) tm ON true";

	/**
	 * Met by then: the subcategory known, of a met type, first appeared up to the chapter
	 * or with no first appearance.
	 */
	private static final String SUBCATEGORY_MET = "LEFT JOIN LATERAL (SELECT true AS met"
			+ " FROM published.devil_fruit_type_subcategory WHERE tm.met AND type_id = r.type_id"
			+ " AND id = r.subcategory_id AND " + PublishedViews.SUBCATEGORY_MET + " LIMIT 1) sm ON true";

	static final String SOURCE = "(SELECT r.id, CASE WHEN r.romaji = r.true_romaji THEN r.slug END AS slug, r.romaji,"
			+ " r.language, r.name, r.description, r.advantages, r.disadvantages,"
			+ met("tm", "type_id", "type_slug", "type_romaji", "type_name") + ", r.image_id, r.published_at, "
			+ met("sm", "subcategory_id", "subcategory_name", "subcategory_description")
			+ ", r.first_appearance_chapter_id, r.first_appearance_chapter_slug, r.first_appearance_chapter_number,"
			+ " r.first_appearance_chapter_title FROM (" + resolved() + ") r " + TYPE_MET + " " + SUBCATEGORY_MET
			+ ") AS devil_fruit";

	private DevilFruitAsOfChapter() {
	}

	/** The fruit with each revealed field as known by then, and its true romaji. */
	private static String resolved() {
		String values = FIELDS.stream()
			.flatMap(field -> field.columns().stream().map(field::resolve))
			.collect(Collectors.joining(", "));
		return "SELECT f.id, f.slug, f.romaji AS true_romaji, f.language, f.image_id, f.published_at,"
				+ " f.first_appearance_chapter_id, f.first_appearance_chapter_slug,"
				+ " f.first_appearance_chapter_number, f.first_appearance_chapter_title, " + values
				+ " FROM published.devil_fruit f LEFT JOIN published.devil_fruit_initial_knowledge k"
				+ " ON k.devil_fruit_id = f.id AND k.language = f.language " + latestReveals();
	}

	/**
	 * The reveals up to the chapter read, in one pass: per field, whether one names it
	 * and the value of the latest that does.
	 */
	private static String latestReveals() {
		String latest = FIELDS.stream().map(RevealedField::latest).collect(Collectors.joining(", "));
		return "LEFT JOIN LATERAL (SELECT " + latest + " FROM published.devil_fruit_reveal x"
				+ " WHERE x.devil_fruit_id = f.id AND x.language = f.language AND x.chapter_id IN " + CHAPTERS_READ
				+ ") x ON true";
	}

	private static String met(String alias, String... columns) {
		return Arrays.stream(columns)
			.map(column -> "CASE WHEN " + alias + ".met THEN r." + column + " END AS " + column)
			.collect(Collectors.joining(", "));
	}

	/**
	 * @param field the field as {@code published.devil_fruit_reveal.fields} names it
	 * @param columns the columns it resolves, the same in the fruit, the initial
	 * knowledge and the reveal views
	 */
	private record RevealedField(String field, List<String> columns) {

		RevealedField(String field, String... columns) {
			this(field, List.of(columns));
		}

		private String found() {
			return this.field.toLowerCase() + "_found";
		}

		private String named() {
			return "'" + this.field + "' = ANY (x.fields)";
		}

		/** Whether a reveal names the field, and each column of the latest that does. */
		String latest() {
			String values = this.columns.stream()
				.map(column -> "(array_agg(x." + column + " ORDER BY x.position DESC) FILTER (WHERE " + named()
						+ "))[1] AS " + column)
				.collect(Collectors.joining(", "));
			return "bool_or(" + named() + ") AS " + found() + ", " + values;
		}

		String resolve(String column) {
			return "CASE WHEN x." + found() + " THEN x." + column + " WHEN '" + this.field
					+ "' = ANY (k.fields) THEN k." + column + " ELSE f." + column + " END AS " + column;
		}

	}

}
