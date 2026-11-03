package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.WindowState;
import dev.onepieceapi.readertime.Windowed;

/**
 * An entry of a list shown inside its window (workflow 3.7.2): a species, an epithet, a
 * period, an image. {@code former} is derived from the window, never stored.
 */
public interface WindowedEntry extends Windowed {

	/** Whether the reader knows it is over: its end is shown. */
	default boolean former() {
		return window().state() == WindowState.FORMER;
	}

}
