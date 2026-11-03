package dev.onepieceapi.publicapi.web.validation;

import dev.onepieceapi.publicapi.web.dto.request.ReaderAxisRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Checks {@link AtMostOneReaderAxis}. The violation names {@code asOfEpisode}, so the
 * error contract lists it with the other field violations, as content-service's preview
 * does.
 */
public class AtMostOneReaderAxisValidator implements ConstraintValidator<AtMostOneReaderAxis, ReaderAxisRequest> {

	private static final String FIELD = "asOfEpisode";

	@Override
	public boolean isValid(ReaderAxisRequest request, ConstraintValidatorContext context) {
		if (request == null || request.asOfChapter() == null || request.asOfEpisode() == null) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
			.addPropertyNode(FIELD)
			.addConstraintViolation();
		return false;
	}

}
