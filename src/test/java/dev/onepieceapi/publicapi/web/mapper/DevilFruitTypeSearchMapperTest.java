package dev.onepieceapi.publicapi.web.mapper;

import dev.onepieceapi.publicapi.config.PublicApiProperties.Pagination;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSearch;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeSort;
import dev.onepieceapi.publicapi.web.dto.request.DevilFruitTypeListRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DevilFruitTypeSearchMapperTest {

	private static final Pagination PAGINATION = new Pagination(20, 100);

	@Test
	void appliesTheDefaultsToAnEmptyRequest() {
		DevilFruitTypeSearch search = toSearch(new DevilFruitTypeListRequest(null, null, null, null));

		assertThat(search).isEqualTo(new DevilFruitTypeSearch(null, DevilFruitTypeSort.DEFAULT, 0, 20));
	}

	@Test
	void capsASizeAboveTheMaximumSilently() {
		assertThat(toSearch(new DevilFruitTypeListRequest(null, 5000, null, null)).size()).isEqualTo(100);
	}

	@Test
	void keepsASizeWithinTheMaximum() {
		assertThat(toSearch(new DevilFruitTypeListRequest(2, 7, null, null)).size()).isEqualTo(7);
	}

	@Test
	void treatsABlankSearchTextAsNoFilterAndStripsTheOthers() {
		assertThat(toSearch(new DevilFruitTypeListRequest(null, null, null, "   ")).text()).isNull();
		assertThat(toSearch(new DevilFruitTypeListRequest(null, null, null, " zoan ")).text()).isEqualTo("zoan");
	}

	private static DevilFruitTypeSearch toSearch(DevilFruitTypeListRequest request) {
		return DevilFruitTypeSearchMapper.toSearch(request, PAGINATION);
	}

}
