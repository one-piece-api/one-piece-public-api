package dev.onepieceapi.publicapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import dev.onepieceapi.publicapi.config.PublicApiProperties;
import dev.onepieceapi.publicapi.domain.DevilFruitTypeLookup;
import dev.onepieceapi.publicapi.service.DevilFruitTypeService;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.dto.request.DevilFruitTypeListRequest;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitTypeResponse;
import dev.onepieceapi.publicapi.web.dto.response.DevilFruitTypeSummaryResponse;
import dev.onepieceapi.publicapi.web.dto.response.PageResponse;
import dev.onepieceapi.publicapi.web.mapper.DevilFruitTypeSearchMapper;
import dev.onepieceapi.publicapi.web.mapper.PublicResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** The Devil Fruit Types online, in the language of the path. */
@RestController
@Tag(name = "Devil Fruit Types")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
class DevilFruitTypeController {

	private final DevilFruitTypeService service;

	private final PublicApiProperties properties;

	@Parameter(in = ParameterIn.QUERY, name = "sort",
			description = "name, romaji or publishedAt, then asc or desc; default name,asc",
			schema = @Schema(type = "string", example = "name,asc"))
	@Operation(description = "Lists the Devil Fruit Types online, one page at a time")
	@GetMapping(ApiPaths.DEVIL_FRUIT_TYPES)
	PageResponse<DevilFruitTypeSummaryResponse> list(@PathVariable String lang,
			@ParameterObject @Valid DevilFruitTypeListRequest request) {
		var search = DevilFruitTypeSearchMapper.toSearch(request, this.properties.pagination());
		return PublicResponseMapper.toResponse(this.service.search(lang, search), PublicResponseMapper::toResponse);
	}

	/**
	 * By id or slug. An old slug answers {@code 301} with the current one as a relative
	 * {@code Location}: it resolves against the request, so it holds behind any prefix
	 * the gateway strips (plan D5, D10).
	 */
	@Operation(description = "One Devil Fruit Type by id or slug")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "301",
			description = "An old slug: Location is the current slug, relative to this address", content = @Content)
	@GetMapping(ApiPaths.DEVIL_FRUIT_TYPE)
	ResponseEntity<DevilFruitTypeResponse> get(@PathVariable String lang, @PathVariable String idOrSlug) {
		return switch (this.service.find(lang, idOrSlug)) {
			case DevilFruitTypeLookup.Found found ->
				ResponseEntity.ok(PublicResponseMapper.toResponse(found.devilFruitType()));
			case DevilFruitTypeLookup.Moved moved -> ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
				.header(HttpHeaders.LOCATION, moved.currentSlug())
				.build();
		};
	}

}
