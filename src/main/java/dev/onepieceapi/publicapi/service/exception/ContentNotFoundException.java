package dev.onepieceapi.publicapi.service.exception;

import dev.onepieceapi.exception.NotFoundException;

/**
 * No content is online at this address: never published, retired, or not a known id or
 * slug - the three look the same from outside (plan D9).
 */
public class ContentNotFoundException extends NotFoundException {

	public ContentNotFoundException(String idOrSlug) {
		super(PublicErrorCode.CONTENT_NOT_FOUND, "Nothing is published at " + idOrSlug);
	}

}
