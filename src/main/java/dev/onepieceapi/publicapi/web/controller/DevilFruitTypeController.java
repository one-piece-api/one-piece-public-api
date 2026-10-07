package dev.onepieceapi.publicapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import dev.onepieceapi.publicapi.config.PublicApiProperties;
import dev.onepieceapi.publicapi.service.DevilFruitTypeService;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.ContentResponses;
import dev.onepieceapi.publicapi.web.dto.request.ContentListRequest;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitTypeResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitTypeSummaryResponse;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

/** The Devil Fruit Types online, in the language of the path. */
@RestController
@Tag(name = "Devil Fruit Types")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
class DevilFruitTypeController {

	private final DevilFruitTypeService service;

	private final PublicApiProperties properties;

	private final ContentResponses responses;

	@Parameter(in = ParameterIn.QUERY, name = "sort",
			description = "name, romaji or publishedAt, then asc or desc; default name,asc",
			schema = @Schema(type = "string", example = "name,asc"))
	@ApiResponse(responseCode = "304", description = "The caller's ETag is current")
	@Operation(description = "Lists the Devil Fruit Types online, one page at a time")
	@GetMapping(ApiPaths.DEVIL_FRUIT_TYPES)
	ResponseEntity<PageResponse<DevilFruitTypeSummaryResponse>> list(@PathVariable String lang,
			@ParameterObject @Valid ContentListRequest request, WebRequest webRequest) {
		var search = ContentSearchMapper.toSearch(request, this.properties.pagination());
		return this.responses.page(webRequest, () -> this.service.search(lang, search),
				PublicResponseMapper::toResponse);
	}

	/** By id or slug; an old slug answers {@code 301} (see {@link ContentResponses}). */
	@Operation(description = "One Devil Fruit Type by id or slug")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "304", description = "The caller's ETag is current")
	@ApiResponse(responseCode = "301",
			description = "An old slug: Location is the current slug, relative to this address", content = @Content)
	@GetMapping(ApiPaths.DEVIL_FRUIT_TYPE)
	ResponseEntity<DevilFruitTypeResponse> get(@PathVariable String lang, @PathVariable String idOrSlug,
			WebRequest webRequest) {
		return this.responses.lookup(webRequest, () -> this.service.find(lang, idOrSlug),
				PublicResponseMapper::toResponse);
	}

}
