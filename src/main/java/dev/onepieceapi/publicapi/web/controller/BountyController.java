package dev.onepieceapi.publicapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import dev.onepieceapi.publicapi.config.PublicApiProperties;
import dev.onepieceapi.publicapi.domain.BountyCondition;
import dev.onepieceapi.publicapi.domain.BountyValidity;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import dev.onepieceapi.publicapi.service.BountyService;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.ContentResponses;
import dev.onepieceapi.publicapi.web.dto.request.ContentListRequest;
import dev.onepieceapi.publicapi.web.dto.response.BountyResponse;
import dev.onepieceapi.publicapi.web.dto.response.BountySummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.PageResponse;
import dev.onepieceapi.publicapi.web.mapper.ContentSearchMapper;
import dev.onepieceapi.publicapi.web.mapper.PublicResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.Optional;

/** The bounties online, in the language of the path (Bounty plan D5). */
@RestController
@Tag(name = "Bounties")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
class BountyController {

	private static final String VALIDITY_DESCRIPTION = "Only the bounties of this validity, derived from the bounties"
			+ " online";

	private static final String CONDITION_DESCRIPTION = "Only the bounties of this condition";

	private final BountyService service;

	private final PublicApiProperties properties;

	private final ContentResponses responses;

	@Parameter(in = ParameterIn.QUERY, name = "sort",
			description = "amount, name (poster name, then amount) or publishedAt, then asc or desc; default amount,desc",
			schema = @Schema(type = "string", example = "amount,desc"))
	@ApiResponse(responseCode = "304", description = "The caller's ETag is current")
	@Operation(description = "Lists the bounties online, one page at a time")
	@GetMapping(ApiPaths.BOUNTIES)
	ResponseEntity<PageResponse<BountySummaryResponse>> list(@PathVariable String lang,
			@ParameterObject @Valid ContentListRequest request,
			@Parameter(description = VALIDITY_DESCRIPTION) @RequestParam Optional<BountyValidity> validity,
			@Parameter(description = CONDITION_DESCRIPTION) @RequestParam Optional<BountyCondition> condition,
			WebRequest webRequest) {
		var filters = ContentSearchMapper
			.toFilters(Map.of(PublishedViews.VALIDITY_FILTER, validity.map(BountyValidity::name),
					PublishedViews.CONDITION_FILTER, condition.map(BountyCondition::name)));
		var search = ContentSearchMapper.toSearch(request, this.properties.pagination(), filters,
				ContentSort.BY_AMOUNT_DESCENDING);
		return this.responses.page(webRequest, () -> this.service.search(lang, search),
				PublicResponseMapper::toResponse);
	}

	/** By id or slug; an old slug answers {@code 301} (see {@link ContentResponses}). */
	@Operation(description = "One bounty by id or slug, with its chain: the bounties it replaces and that replace it,"
			+ " newest first")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "304", description = "The caller's ETag is current")
	@ApiResponse(responseCode = "301",
			description = "An old slug: Location is the current slug, relative to this address", content = @Content)
	@GetMapping(ApiPaths.BOUNTY)
	ResponseEntity<BountyResponse> get(@PathVariable String lang, @PathVariable String idOrSlug,
			WebRequest webRequest) {
		return this.responses.lookup(webRequest, () -> this.service.find(lang, idOrSlug),
				PublicResponseMapper::toResponse);
	}

}
