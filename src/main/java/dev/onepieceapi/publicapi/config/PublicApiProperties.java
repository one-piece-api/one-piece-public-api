package dev.onepieceapi.publicapi.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The API's tunables, from {@code public-api.*} (plan D8).
 *
 * @param pagination the page sizes of every list
 */
@Validated
@ConfigurationProperties("public-api")
public record PublicApiProperties(Pagination pagination) {

	/**
	 * @param defaultSize the size of a page when the caller does not ask for one
	 * @param maxSize the largest page served: a larger {@code size} is capped to it
	 */
	public record Pagination(@Positive int defaultSize, @Positive int maxSize) {

	}

}
