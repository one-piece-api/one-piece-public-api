package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A Character inside the content that names it, such as the holder of a bounty. The name
 * is null when the character is not written in the language.
 */
public record CharacterReference(UUID id, String slug, String name) {

	/**
	 * A link once the reader has met the character, named as known at N; null before, or
	 * for no link (workflow 3.8).
	 */
	public static CharacterReference seenBy(CharacterReference link, ReaderSight sight) {
		CharacterReference seen = sight.ifSeen(link, CharacterReference::id);
		return (seen != null) ? seen.namedBy(sight) : null;
	}

	/** Named and addressed as the reader knows it at N (reveals plan R7 S10). */
	public CharacterReference namedBy(ReaderSight sight) {
		CharacterValues known = sight.characterAt(this.id, new CharacterValues(this.name, null, this.slug, null, null));
		return new CharacterReference(this.id, known.slug(), known.name());
	}

}
