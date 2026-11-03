package dev.onepieceapi.publicapi.domain;

/**
 * The revealable values of a Character (workflow 3.7.1); a field not held is null.
 *
 * @param name in the reader's language
 * @param slug the address derived from the romaji; null when it leaves none
 */
public record CharacterValues(String name, String romaji, String slug, String japaneseName,
		CharacterStatus status) implements RevealedValues<CharacterField, CharacterValues> {

	@Override
	public boolean holds(CharacterField field) {
		return switch (field) {
			case IDENTITY -> this.name != null || this.romaji != null || this.japaneseName != null;
			case STATUS -> this.status != null;
		};
	}

	@Override
	public CharacterValues withField(CharacterField field, CharacterValues other) {
		return switch (field) {
			case IDENTITY -> new CharacterValues(other.name, other.romaji, other.slug, other.japaneseName, this.status);
			case STATUS -> new CharacterValues(this.name, this.romaji, this.slug, this.japaneseName, other.status);
		};
	}

}
