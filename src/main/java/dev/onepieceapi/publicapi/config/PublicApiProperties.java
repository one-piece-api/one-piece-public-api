package dev.onepieceapi.publicapi.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * The API's tunables, from {@code public-api.*} (plan D8, D13).
 *
 * @param pagination the page sizes of every list
 * @param cache how long each kind of answer may be reused
 */
@Validated
@ConfigurationProperties("public-api")
public record PublicApiProperties(@Valid Pagination pagination, @Valid Cache cache) {

	/**
	 * @param defaultSize the size of a page when the caller does not ask for one
	 * @param maxSize the largest page served: a larger {@code size} is capped to it
	 */
	public record Pagination(@Positive int defaultSize, @Positive int maxSize) {

	}

	/**
	 * @param okMaxAge how long a {@code 200} is fresh
	 * @param okStaleWhileRevalidate how long after that it may still be served while it
	 * is revalidated: with {@code okMaxAge}, the worst case of a stale answer
	 * @param notFoundMaxAge how long a {@code 404} is reused
	 * @param movedMaxAge how long a {@code 301} is reused: explicit, as browsers keep a
	 * bare one forever and a romaji changed back would loop
	 */
	public record Cache(@NotNull Duration okMaxAge, @NotNull Duration okStaleWhileRevalidate,
			@NotNull Duration notFoundMaxAge, @NotNull Duration movedMaxAge) {

	}

}
