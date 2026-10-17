package dev.onepieceapi.publicapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import dev.onepieceapi.publicapi.domain.AsOfChapter;
import dev.onepieceapi.publicapi.config.PublicApiProperties;
import dev.onepieceapi.publicapi.domain.ContentSort;
import dev.onepieceapi.publicapi.service.SagaService;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.ContentResponses;
import dev.onepieceapi.publicapi.web.dto.request.ContentListRequest;
import dev.onepieceapi.publicapi.web.dto.response.PageResponse;
import dev.onepieceapi.publicapi.web.dto.response.SagaResponse;
import dev.onepieceapi.publicapi.web.dto.response.SagaSummaryResponse;
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

import java.util.List;

/** The sagas online, in the language of the path (story structure plan S9). */
@RestController
@Tag(name = "Sagas")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
class SagaController {

	private final SagaService service;

	private final PublicApiProperties properties;

	private final ContentResponses responses;

	@Parameter(in = ParameterIn.QUERY, name = "sort",
			description = "name, number or publishedAt, then asc or desc; default number,asc",
			schema = @Schema(type = "string", example = "number,asc"))
	@ApiResponse(responseCode = "304", description = "The caller's ETag is current")
	@Operation(description = "Lists the sagas online, one page at a time")
	@GetMapping(ApiPaths.SAGAS)
	ResponseEntity<PageResponse<SagaSummaryResponse>> list(@PathVariable String lang,
			@ParameterObject @Valid ContentListRequest request, WebRequest webRequest) {
		var search = ContentSearchMapper.toSearch(request, this.properties.pagination(), List.of(),
				ContentSort.BY_NUMBER);
		return this.responses.page(webRequest, () -> this.service.search(lang, search),
				PublicResponseMapper::toResponse);
	}

	/**
	 * By id or slug (the number); an old slug answers {@code 301} (see
	 * {@link ContentResponses}).
	 */
	@Operation(description = "One saga by id or number, with its arcs and what its chapters and episodes amount to")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "304", description = "The caller's ETag is current")
	@ApiResponse(responseCode = "301",
			description = "An old slug: Location is the current slug, relative to this address", content = @Content)
	@GetMapping(ApiPaths.SAGA)
	ResponseEntity<SagaResponse> get(@PathVariable String lang, @PathVariable String idOrSlug, WebRequest webRequest) {
		return this.responses.lookup(webRequest, () -> this.service.find(lang, idOrSlug, AsOfChapter.NONE),
				PublicResponseMapper::toResponse);
	}

}
