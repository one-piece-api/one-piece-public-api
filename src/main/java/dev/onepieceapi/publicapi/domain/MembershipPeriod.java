package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.RevealWindow;

/**
 * One stay of a character in an affiliation, inside its window (workflow 3.4.7, 3.7.2).
 *
 * @param role in the reader's language; null when none is written in it
 * @param marineRank null for none
 */
public record MembershipPeriod(String role, MarineRank marineRank, RevealWindow window) implements WindowedEntry {

}
