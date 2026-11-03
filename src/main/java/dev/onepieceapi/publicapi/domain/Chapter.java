package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A chapter online, in one language.
 *
 * @param id the content's identifier
 * @param slug the number, as text
 * @param japaneseTitle the title as published in Japan, one for every language; null when
 * unknown
 * @param pageCount null when unknown
 * @param arc its arc, in the same language
 * @param volume its volume; null when it is not collected in one yet
 * @param releases its release day in every country it has one; empty until added
 * @param publishedAt when the online version was published
 */
@Builder(toBuilder = true)
public record Chapter(UUID id, String slug, int number, String language, String title, String description,
		String japaneseTitle, Integer pageCount, ArcReference arc, VolumeSummary volume, @With List<Release> releases,
		Instant publishedAt) implements PublishedContent, SeenByReader<Chapter> {

	public Chapter {
		releases = releases == null ? List.of() : List.copyOf(releases);
	}

	@Override
	public Chapter seenBy(ReaderSight sight) {
		return toBuilder().arc(sight.ifSeen(this.arc, ArcReference::id))
			.volume(sight.ifSeen(this.volume, VolumeSummary::id))
			.build();
	}

}
