package dev.onepieceapi.publicapi.service.exception;

import dev.onepieceapi.exception.CommonErrorCode;
import dev.onepieceapi.exception.ValidationException;
import dev.onepieceapi.publicapi.domain.ContentSort;

/**
 * The list cannot be ordered by that field: one only some entities declare, such as the
 * amount of a bounty.
 */
public class SortNotAvailableException extends ValidationException {

	public SortNotAvailableException(ContentSort.Field field) {
		super(CommonErrorCode.VALIDATION_FAILED, "This list cannot be sorted by " + field.getRequestName());
	}

}
