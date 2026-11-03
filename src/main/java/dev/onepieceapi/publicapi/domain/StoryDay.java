package dev.onepieceapi.publicapi.domain;

import java.time.LocalDate;

/**
 * A chapter or an episode online as a span counts it: its number and its Japanese release
 * (or broadcast) day.
 *
 * @param number the chapter or episode number
 * @param date the Japanese day; null when unknown
 */
public record StoryDay(int number, LocalDate date) {

}
