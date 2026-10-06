package dev.onepieceapi.publicapi.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageTest {

	@Test
	void roundsThePagesUp() {
		assertThat(new Page<>(List.of(), 0, 20, 41).totalPages()).isEqualTo(3);
		assertThat(new Page<>(List.of(), 0, 20, 40).totalPages()).isEqualTo(2);
	}

	@Test
	void hasNoPagesWhenEmpty() {
		assertThat(new Page<>(List.of(), 0, 20, 0).totalPages()).isZero();
	}

}
