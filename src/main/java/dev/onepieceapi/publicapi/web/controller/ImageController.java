package dev.onepieceapi.publicapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import dev.onepieceapi.publicapi.config.PublicApiProperties;
import dev.onepieceapi.publicapi.domain.Image;
import dev.onepieceapi.publicapi.service.ImageService;
import dev.onepieceapi.publicapi.web.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * The images of the contents online (Devil Fruit plan D13). An image never changes at its
 * address, its id being the hash of its bytes: cached for long, immutable, no ETag.
 */
@RestController
@Tag(name = "Images")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
class ImageController {

	private final ImageService service;

	private final PublicApiProperties properties;

	@Operation(description = "An image of a content online, by the id its content gives")
	@ApiResponse(responseCode = "200", description = "OK",
			content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
					schema = @Schema(type = "string", format = "binary")))
	@GetMapping(ApiPaths.IMAGE)
	ResponseEntity<byte[]> get(@PathVariable String id) {
		Image image = this.service.find(id);
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(image.contentType()))
			.cacheControl(CacheControl.maxAge(this.properties.cache().imageMaxAge()).cachePublic().immutable())
			.body(image.bytes());
	}

}
