package dev.onepieceapi.publicapi.service.exception;

import dev.onepieceapi.exception.NotFoundException;

/** The language in the path is not one of the public languages. */
public class LanguageNotAvailableException extends NotFoundException {

	public LanguageNotAvailableException(String language) {
		super(PublicErrorCode.LANGUAGE_NOT_AVAILABLE, "Language " + language + " is not available");
	}

}
