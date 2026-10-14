package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * An episode online, in one language.
 *
 * @param id the content's identifier
 * @param slug the number, as text
 * @param japaneseTitle the title as published in Japan, one for every language; null when
 * unknown
 * @param arc its arc; null for an episode outside any arc (a filler)
 * @param broadcasts its broadcast day in every country it has one; empty until added
 * @param publishedAt when the online version was published
 */
@Builder
public record Episode(UUID id, String slug, int number, String language, String title, String description,
		String japaneseTitle, ArcReference arc, @With List<Release> broadcasts,
		Instant publishedAt) implements PublishedContent {

	public Episode {
		broadcasts = broadcasts == null ? List.of() : List.copyOf(broadcasts);
	}

}
