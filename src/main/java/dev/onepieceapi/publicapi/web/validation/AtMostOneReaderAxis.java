package dev.onepieceapi.publicapi.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * On the spoiler filter: a chapter or an episode, never both (reveals plan D12). Checked
 * by {@link AtMostOneReaderAxisValidator}.
 */
@Documented
@Constraint(validatedBy = AtMostOneReaderAxisValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AtMostOneReaderAxis {

	String message() default "asOfChapter and asOfEpisode cannot be given together";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
