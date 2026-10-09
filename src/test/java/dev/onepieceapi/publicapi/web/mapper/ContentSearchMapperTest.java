package dev.onepieceapi.publicapi.web.mapper;

import dev.onepieceapi.publicapi.config.PublicApiProperties.Pagination;
import dev.onepieceapi.publicapi.domain.ContentSearch;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.web.dto.request.ContentListRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentSearchMapperTest {

	private static final Pagination PAGINATION = new Pagination(20, 100);

	@Test
	void appliesTheDefaultsToAnEmptyRequest() {
		ContentSearch search = toSearch(new ContentListRequest(null, null, null, null));

		assertThat(search).isEqualTo(ContentSearch.builder().sort(ContentSort.DEFAULT).page(0).size(20).build());
	}

	@Test
	void capsASizeAboveTheMaximumSilently() {
		assertThat(toSearch(new ContentListRequest(null, 5000, null, null)).size()).isEqualTo(100);
	}

	@Test
	void keepsASizeWithinTheMaximum() {
		assertThat(toSearch(new ContentListRequest(2, 7, null, null)).size()).isEqualTo(7);
	}

	@Test
	void treatsABlankSearchTextAsNoFilterAndStripsTheOthers() {
		assertThat(toSearch(new ContentListRequest(null, null, null, "   ")).text()).isNull();
		assertThat(toSearch(new ContentListRequest(null, null, null, " zoan ")).text()).isEqualTo("zoan");
	}

	private static ContentSearch toSearch(ContentListRequest request) {
		return ContentSearchMapper.toSearch(request, PAGINATION);
	}

}
