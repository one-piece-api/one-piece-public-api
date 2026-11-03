package dev.onepieceapi.publicapi.domain;

import lombok.experimental.UtilityClass;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * The order of names in a language when the spoiler filter sorts in memory (reveals plan
 * R7 S12, S13): close to the database's, which sorts without the filter, but not equal on
 * every space or punctuation mark. A missing name comes last.
 */
@UtilityClass
public class NameOrder {

	/** The order of names written in the language. */
	public Comparator<String> in(String language) {
		Collator collator = Collator.getInstance(Locale.forLanguageTag(language));
		return Comparator.nullsLast(collator::compare);
	}

}
