package dev.onepieceapi.publicapi.config;

import dev.onepieceapi.publicapi.web.converter.DevilFruitTypeSortConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * What the browser and the request binding need (plan D12): any origin may read, with
 * {@code GET} / {@code HEAD} only and no credentials, and the {@code sort} parameter is
 * converted by the framework's own conversion service.
 */
@Configuration
class WebConfig implements WebMvcConfigurer {

	/** How long a browser may reuse a preflight answer. */
	private static final long PREFLIGHT_MAX_AGE_SECONDS = 86_400;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins("*")
			.allowedMethods("GET", "HEAD")
			.exposedHeaders(HttpHeaders.ETAG, HttpHeaders.RETRY_AFTER, HttpHeaders.LOCATION)
			.allowCredentials(false)
			.maxAge(PREFLIGHT_MAX_AGE_SECONDS);
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new DevilFruitTypeSortConverter());
	}

}
