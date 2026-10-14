package dev.onepieceapi.publicapi.domain;

import java.time.LocalDate;

/**
 * What the chapters (or the episodes) online of a parent amount to, derived from them
 * (story structure plan S7): never stored, never a list of them.
 *
 * @param count how many are online
 * @param firstNumber the lowest number; null when none
 * @param lastNumber the highest number; null when none
 * @param firstDate the earliest Japanese release (or broadcast) day among them; null when
 * none has one
 * @param lastDate the latest one; null when none has one
 */
public record StorySpan(int count, Integer firstNumber, Integer lastNumber, LocalDate firstDate, LocalDate lastDate) {

}
