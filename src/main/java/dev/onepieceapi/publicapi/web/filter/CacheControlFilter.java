package dev.onepieceapi.publicapi.web.filter;

import dev.onepieceapi.publicapi.config.PublicApiProperties;
import dev.onepieceapi.publicapi.config.PublicApiProperties.Cache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * Puts the {@code Cache-Control} of the cache policy (plan D13) on every {@code GET} and
 * {@code HEAD} answer, by status, errors included. The status is only final once the
 * request is done, so the body is held back until the header is set: it is small (a page
 * of at most 100 rows). A {@code 304} repeats the header of the {@code 200} it stands
 * for.
 */
@Component
class CacheControlFilter extends OncePerRequestFilter {

	private final CacheControl ok;

	private final CacheControl notFound;

	private final CacheControl moved;

	CacheControlFilter(PublicApiProperties properties) {
		Cache cache = properties.cache();
		this.ok = CacheControl.maxAge(cache.okMaxAge())
			.cachePublic()
			.staleWhileRevalidate(cache.okStaleWhileRevalidate());
		this.notFound = CacheControl.maxAge(cache.notFoundMaxAge()).cachePublic();
		this.moved = CacheControl.maxAge(cache.movedMaxAge()).cachePublic();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (!isRead(request)) {
			chain.doFilter(request, response);
			return;
		}
		ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
		try {
			chain.doFilter(request, wrapper);
		}
		finally {
			if (!wrapper.containsHeader(HttpHeaders.CACHE_CONTROL)) {
				wrapper.setHeader(HttpHeaders.CACHE_CONTROL, policyFor(wrapper.getStatus()).getHeaderValue());
			}
			wrapper.copyBodyToResponse();
		}
	}

	private CacheControl policyFor(int status) {
		return switch (HttpStatus.resolve(status)) {
			case OK, NOT_MODIFIED -> this.ok;
			case NOT_FOUND -> this.notFound;
			case MOVED_PERMANENTLY -> this.moved;
			case null, default -> CacheControl.noStore();
		};
	}

	private static boolean isRead(HttpServletRequest request) {
		return HttpMethod.GET.matches(request.getMethod()) || HttpMethod.HEAD.matches(request.getMethod());
	}

}
