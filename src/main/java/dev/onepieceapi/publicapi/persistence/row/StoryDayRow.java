package dev.onepieceapi.publicapi.persistence.row;

import java.time.LocalDate;

/** A chapter or an episode online with its Japanese day, as a span counts it. */
public record StoryDayRow(int number, LocalDate date) {

}
