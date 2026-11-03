package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.RevealWindow;

/**
 * An epithet or an alias of a character, inside its window (workflow 3.7.2).
 *
 * @param value the text, in the language of the path for an epithet, the same in every
 * language for an alias
 */
public record TextEntry(String value, RevealWindow window) implements WindowedEntry {

}
