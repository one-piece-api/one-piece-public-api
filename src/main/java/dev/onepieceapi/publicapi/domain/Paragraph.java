package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.RevealWindow;
import dev.onepieceapi.readertime.Windowed;
import lombok.With;

/**
 * A paragraph of a long text, inside its window (workflow 3.7.3).
 *
 * @param text in the reader's language
 */
public record Paragraph(String text, @With RevealWindow window) implements Windowed {

}
