package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.util.UUID;

/**
 * A Character as a row of the list.
 *
 * @param imageId the main portrait's identifier (workflow 3.5.1); null when it has none
 */
@Builder(toBuilder = true)
public record CharacterSummary(UUID id, String slug, String romaji, String name, CharacterStatus status,
		@With String imageId) implements SeenByReader<CharacterSummary> {

	/** Named, addressed and with its status as known at N (workflow 3.7.1). */
	@Override
	public CharacterSummary seenBy(ReaderSight sight) {
		CharacterValues known = sight.characterAt(this.id,
				new CharacterValues(this.name, this.romaji, this.slug, null, this.status));
		return toBuilder().name(known.name()).romaji(known.romaji()).slug(known.slug()).status(known.status()).build();
	}

}
