package dev.onepieceapi.publicapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import dev.onepieceapi.publicapi.web.ApiPaths;
import dev.onepieceapi.publicapi.web.dto.response.IndexResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.TreeMap;

/** The entry point of the API: where everything else is, relative to the API root. */
@RestController
@Tag(name = "Index")
class IndexController {

	@Operation(description = "The entry point: where the other parts of the API are")
	@GetMapping(ApiPaths.V1)
	IndexResponse index() {
		return new IndexResponse(relative(ApiPaths.LANGUAGES),
				new TreeMap<>(Map.ofEntries(link("devil-fruit-types", ApiPaths.DEVIL_FRUIT_TYPES),
						link("devil-fruits", ApiPaths.DEVIL_FRUITS), link("sagas", ApiPaths.SAGAS),
						link("arcs", ApiPaths.ARCS), link("volumes", ApiPaths.VOLUMES),
						link("chapters", ApiPaths.CHAPTERS), link("episodes", ApiPaths.EPISODES),
						link("bounties", ApiPaths.BOUNTIES), link("affiliations", ApiPaths.AFFILIATIONS),
						link("species", ApiPaths.SPECIES), link("characters", ApiPaths.CHARACTERS))));
	}

	private static Map.Entry<String, String> link(String resource, String path) {
		return Map.entry(resource, relative(path));
	}

	/** A path as a template relative to the API root: no leading slash. */
	private static String relative(String path) {
		return path.substring(1);
	}

}
