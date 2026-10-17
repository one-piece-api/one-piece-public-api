package dev.onepieceapi.publicapi.persistence.row;

import java.time.LocalDate;

/** What the chapters online of a parent amount to, as computed on the published views. */
public record StorySpanRow(int count, Integer firstNumber, Integer lastNumber, LocalDate firstDate,
		LocalDate lastDate) {

}
