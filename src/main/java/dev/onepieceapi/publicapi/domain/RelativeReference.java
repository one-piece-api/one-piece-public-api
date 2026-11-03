package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * A character inside the character that names it as a relative, or that is named by it.
 * The gender gives the label of the kind (Character plan D13).
 */
public record RelativeReference(UUID id, String slug, String name, Gender gender) {

	/** Named and addressed as the reader knows it at N (reveals plan R7 S10). */
	public RelativeReference namedBy(ReaderSight sight) {
		CharacterValues known = sight.characterAt(this.id, new CharacterValues(this.name, null, this.slug, null, null));
		return new RelativeReference(this.id, known.slug(), known.name(), this.gender);
	}

}
