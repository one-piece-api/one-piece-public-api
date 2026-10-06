package dev.onepieceapi.publicapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import dev.onepieceapi.publicapi.service.LanguageService;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.dto.response.LanguageResponse;
import dev.onepieceapi.publicapi.web.mapper.PublicResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The languages the content is available in. */
@RestController
@Tag(name = "Languages")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
class LanguageController {

	private final LanguageService service;

	@Operation(description = "The languages the content is available in")
	@GetMapping(ApiPaths.LANGUAGES)
	List<LanguageResponse> list() {
		return this.service.list().stream().map(PublicResponseMapper::toResponse).toList();
	}

}
