package dev.onepieceapi.publicapi.web.dto.response;

import java.time.LocalDate;

/**
 * What the chapters (or the episodes) online of a parent amount to, derived from them and
 * never a list of them: reach the items through the {@code arc}, {@code saga} or
 * {@code volume} filter of their section.
 *
 * @param count how many are online
 * @param firstNumber the lowest number; {@code null} when none
 * @param lastNumber the highest number; {@code null} when none
 * @param firstDate the earliest Japanese (JP) release or broadcast day among them;
 * {@code null} when none has one
 * @param lastDate the latest one; {@code null} when none has one
 */
public record StorySpanResponse(int count, Integer firstNumber, Integer lastNumber, LocalDate firstDate,
		LocalDate lastDate) {

}
