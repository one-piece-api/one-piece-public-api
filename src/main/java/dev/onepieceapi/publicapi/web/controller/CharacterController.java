package dev.onepieceapi.publicapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import dev.onepieceapi.publicapi.config.PublicApiProperties;
import dev.onepieceapi.publicapi.domain.CharacterStatus;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import dev.onepieceapi.publicapi.service.CharacterService;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.ContentResponses;
import dev.onepieceapi.publicapi.web.dto.request.ContentListRequest;
import dev.onepieceapi.publicapi.web.dto.response.CharacterResponse;
import dev.onepieceapi.publicapi.web.dto.response.CharacterSummaryResponse;
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

/** The characters online, in the language of the path (Character plan D16). */
@RestController
@Tag(name = "Characters")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
class CharacterController {

	private static final String STATUS_DESCRIPTION = "Only the characters of this status";

	private static final String SPECIES_DESCRIPTION = "Only the characters of this species, by id or slug (current"
			+ " or old); an unknown species lists nothing";

	private final CharacterService service;

	private final PublicApiProperties properties;

	private final ContentResponses responses;

	@Parameter(in = ParameterIn.QUERY, name = "sort",
			description = "name, romaji or publishedAt, then asc or desc; default name,asc",
			schema = @Schema(type = "string", example = "name,asc"))
	@ApiResponse(responseCode = "304", description = "The caller's ETag is current")
	@Operation(description = "Lists the characters online, one page at a time")
	@GetMapping(ApiPaths.CHARACTERS)
	ResponseEntity<PageResponse<CharacterSummaryResponse>> list(@PathVariable String lang,
			@ParameterObject @Valid ContentListRequest request,
			@Parameter(description = STATUS_DESCRIPTION) @RequestParam Optional<CharacterStatus> status,
			@Parameter(description = SPECIES_DESCRIPTION) @RequestParam Optional<String> species,
			WebRequest webRequest) {
		var filters = ContentSearchMapper.toFilters(Map.of(PublishedViews.STATUS_FILTER,
				status.map(CharacterStatus::name), PublishedViews.SPECIES_FILTER, species));
		var search = ContentSearchMapper.toSearch(request, this.properties.pagination(), filters);
		return this.responses.page(webRequest, () -> this.service.search(lang, search),
				PublicResponseMapper::toResponse);
	}

	/** By id or slug; an old slug answers {@code 301} (see {@link ContentResponses}). */
	@Operation(description = "One character by id or slug, with its epithets, aliases and species")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "304", description = "The caller's ETag is current")
	@ApiResponse(responseCode = "301",
			description = "An old slug: Location is the current slug, relative to this address", content = @Content)
	@GetMapping(ApiPaths.CHARACTER)
	ResponseEntity<CharacterResponse> get(@PathVariable String lang, @PathVariable String idOrSlug,
			WebRequest webRequest) {
		return this.responses.lookup(webRequest, () -> this.service.find(lang, idOrSlug),
				PublicResponseMapper::toResponse);
	}

}
