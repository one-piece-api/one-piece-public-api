package dev.onepieceapi.publicapi.web;

import dev.onepieceapi.publicapi.domain.ContentLookup;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.web.dto.response.PageResponse;
import dev.onepieceapi.publicapi.web.mapper.PublicResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The answers every entity's endpoints give the same way: a conditional GET (plan D13)
 * that serves a page as {@code 200} and a lookup as {@code 200} or, for an old slug,
 * {@code 301}.
 */
@Component
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ContentResponses {

	private final ConditionalGet conditionalGet;

	public <S, R> ResponseEntity<PageResponse<R>> page(WebRequest request, Supplier<Page<S>> query,
			Function<S, R> toResponse) {
		return this.conditionalGet.get(request, query,
				(page, eTag) -> ResponseEntity.ok().eTag(eTag).body(PublicResponseMapper.toResponse(page, toResponse)));
	}

	/**
	 * An old slug answers with the current one as a relative {@code Location}: it
	 * resolves against the request, so it holds behind any prefix the gateway strips
	 * (plan D5, D10).
	 */
	public <D, R> ResponseEntity<R> lookup(WebRequest request, Supplier<ContentLookup<D>> query,
			Function<D, R> toResponse) {
		return this.conditionalGet.get(request, query, (lookup, eTag) -> switch (lookup) {
			case ContentLookup.Found<D> found -> ResponseEntity.ok().eTag(eTag).body(toResponse.apply(found.content()));
			case ContentLookup.Moved<D> moved -> ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
				.header(HttpHeaders.LOCATION, moved.currentSlug())
				.build();
		});
	}

}
