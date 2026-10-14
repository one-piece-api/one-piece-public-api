package dev.onepieceapi.publicapi.persistence.row;

import java.time.LocalDate;

/** A country and a day of {@code published.volume_release} and its siblings. */
public record ReleaseRow(String country, LocalDate date) {

}
