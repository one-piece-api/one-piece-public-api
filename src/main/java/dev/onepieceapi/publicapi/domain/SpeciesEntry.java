package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.RevealWindow;

import java.util.UUID;

/** A species of a character online, inside its window (workflow 3.7.2). */
public record SpeciesEntry(SpeciesReference species, RevealWindow window) implements WindowedEntry {

	/** The species named. */
	public UUID speciesId() {
		return this.species.id();
	}

}
