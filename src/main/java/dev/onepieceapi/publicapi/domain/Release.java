package dev.onepieceapi.publicapi.domain;

import java.time.LocalDate;

/**
 * The day a volume or chapter was released, or an episode broadcast, in one country
 * (story structure plan S13).
 *
 * @param country the ISO 3166-1 alpha-2 code
 * @param date the local calendar day in that country
 */
public record Release(String country, LocalDate date) {

}
