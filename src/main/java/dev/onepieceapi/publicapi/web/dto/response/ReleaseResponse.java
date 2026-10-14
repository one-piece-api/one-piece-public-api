package dev.onepieceapi.publicapi.web.dto.response;

import java.time.LocalDate;

/**
 * The day a volume or chapter was released, or an episode broadcast, in one country.
 *
 * @param country the ISO 3166-1 alpha-2 code
 * @param date the local calendar day in that country
 */
public record ReleaseResponse(String country, LocalDate date) {

}
