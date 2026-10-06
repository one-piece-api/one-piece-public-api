package dev.onepieceapi.publicapi.web.controller;

import dev.onepieceapi.publicapi.service.LanguageService;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.ConditionalGet;
import dev.onepieceapi.publicapi.web.dto.response.LanguageResponse;
import dev.onepieceapi.publicapi.web.mapper.PublicResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

/** The languages the content is available in. */
@RestController
@Tag(name = "Languages")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
class LanguageController {

	private final LanguageService service;

	private final ConditionalGet conditionalGet;

	@Operation(description = "The languages the content is available in")
	@ApiResponse(responseCode = "304", description = "The caller's ETag is current")
	@GetMapping(ApiPaths.LANGUAGES)
	ResponseEntity<List<LanguageResponse>> list(WebRequest request) {
		return this.conditionalGet.get(request, this.service::list,
				(languages, eTag) -> ResponseEntity.ok()
					.eTag(eTag)
					.body(languages.stream().map(PublicResponseMapper::toResponse).toList()));
	}

}
