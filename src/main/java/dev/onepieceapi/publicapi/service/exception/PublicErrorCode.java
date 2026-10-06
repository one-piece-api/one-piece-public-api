package dev.onepieceapi.publicapi.service.exception;

import dev.onepieceapi.exception.ErrorCode;

/**
 * The error codes of the public contract (plan D9), on the wire exactly as named: no
 * service prefix, since a client of this API talks to nothing else.
 */
public enum PublicErrorCode implements ErrorCode {

	LANGUAGE_NOT_AVAILABLE, CONTENT_NOT_FOUND;

	@Override
	public String code() {
		return name();
	}

}
