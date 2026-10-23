package dev.onepieceapi.publicapi.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** The kinds of relative and the link read from the other side (Character plan D13). */
class RelativeTest {

	@ParameterizedTest
	@CsvSource({ "PARENT,CHILD", "CHILD,PARENT", "GRANDPARENT,GRANDCHILD", "GRANDCHILD,GRANDPARENT",
			"UNCLE_AUNT,NEPHEW_NIECE", "NEPHEW_NIECE,UNCLE_AUNT", "ADOPTIVE_PARENT,ADOPTIVE_CHILD",
			"ADOPTIVE_CHILD,ADOPTIVE_PARENT", "SIBLING,SIBLING", "COUSIN,COUSIN", "SPOUSE,SPOUSE",
			"SWORN_SIBLING,SWORN_SIBLING" })
	void eachKindReadsFromTheOtherSideAsItsInverse(RelativeKind kind, RelativeKind inverse) {
		assertThat(kind.inverse()).isEqualTo(inverse);
	}

	@ParameterizedTest
	@EnumSource(RelativeKind.class)
	void theInverseOfTheInverseIsTheKindItself(RelativeKind kind) {
		assertThat(kind.inverse().inverse()).isEqualTo(kind);
	}

	@ParameterizedTest
	@EnumSource(RelativeKind.class)
	void anInvertedLinkKeepsTheCharacterAndInvertsTheKind(RelativeKind kind) {
		var character = new RelativeReference(UUID.randomUUID(), "garp", "Garp", Gender.MALE);

		var inverted = new Relative(character, kind).inverted();

		assertThat(inverted.character()).isEqualTo(character);
		assertThat(inverted.kind()).isEqualTo(kind.inverse());
	}

}
