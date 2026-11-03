package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.Point;
import dev.onepieceapi.readertime.ReaderPosition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Values and slugs known at N (workflow 3.7.1, 3.8; reveals plan D13). The manga reveals
 * a false name at chapter 10 and the current one at chapter 20; the anime only the
 * current one, at episode 5. Status is named by a reveal at chapter 30 with no initial
 * value.
 */
class FieldRevealsTest {

	private static final CharacterValues CURRENT = new CharacterValues("Sabo", "Sabo", "sabo", null,
			CharacterStatus.ALIVE);

	private static final CharacterValues INITIAL = new CharacterValues("Masked Man", "Kamen no Otoko", "kamen-no-otoko",
			null, null);

	private static final CharacterValues FALSE_NAME = new CharacterValues("Lucy", "Lucy", "lucy", null, null);

	private static final FieldReveals<CharacterField, CharacterValues> REVEALS = new FieldReveals<>(INITIAL,
			List.of(reveal(new Point(10, null), CharacterField.IDENTITY, FALSE_NAME),
					reveal(Point.of(20, 5), CharacterField.IDENTITY, CharacterValues.UNKNOWN),
					reveal(Point.ofChapter(30), CharacterField.STATUS, CharacterValues.UNKNOWN)));

	@Test
	void eachAxisReadsTheLatestRevealItHasReached() {
		assertThat(nameAt(ReaderPosition.atChapter(9))).isEqualTo("Masked Man");
		assertThat(nameAt(ReaderPosition.atChapter(10))).isEqualTo("Lucy");
		assertThat(nameAt(ReaderPosition.atChapter(20))).isEqualTo("Sabo");
		assertThat(nameAt(ReaderPosition.atEpisode(4))).isEqualTo("Masked Man");
		assertThat(nameAt(ReaderPosition.atEpisode(5))).isEqualTo("Sabo");
	}

	@Test
	void aFieldWithoutAnInitialValueIsUnknownBeforeItsReveal() {
		assertThat(at(ReaderPosition.atChapter(29)).status()).isNull();
		assertThat(at(ReaderPosition.atChapter(30)).status()).isEqualTo(CharacterStatus.ALIVE);
	}

	@Test
	void aContentWithoutRevealsKeepsItsCurrentValues() {
		assertThat(FieldReveals.<CharacterField, CharacterValues>none()
			.valuesAt(CURRENT, CharacterValues.UNKNOWN, ReaderPosition.atChapter(1))).isEqualTo(CURRENT);
	}

	@Test
	void theSlugsKnownAreThoseOfTheInitialAndOfTheRevealsReached() {
		assertThat(slugsAt(ReaderPosition.atChapter(9))).containsExactly("kamen-no-otoko");
		assertThat(slugsAt(ReaderPosition.atChapter(20))).containsExactlyInAnyOrder("kamen-no-otoko", "lucy", "sabo");
		assertThat(slugsAt(ReaderPosition.atEpisode(5))).containsExactlyInAnyOrder("kamen-no-otoko", "sabo");
	}

	@Test
	void withoutARevealOfTheFieldOnlyTheCurrentSlugIsKnown() {
		assertThat(FieldReveals.<CharacterField, CharacterValues>none()
			.knownAt(CharacterField.IDENTITY, CharacterValues::slug, "sabo", ReaderPosition.atChapter(1)))
			.containsExactly("sabo");
	}

	private static String nameAt(ReaderPosition position) {
		return at(position).name();
	}

	private static CharacterValues at(ReaderPosition position) {
		return REVEALS.valuesAt(CURRENT, CharacterValues.UNKNOWN, position);
	}

	private static Set<String> slugsAt(ReaderPosition position) {
		return REVEALS.knownAt(CharacterField.IDENTITY, CharacterValues::slug, "sabo", position);
	}

	private static Reveal<CharacterField, CharacterValues> reveal(Point point, CharacterField field,
			CharacterValues values) {
		return new Reveal<>("title", point, Set.of(field), values);
	}

}
