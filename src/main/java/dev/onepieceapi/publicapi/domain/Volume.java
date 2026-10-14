package dev.onepieceapi.publicapi.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A volume online, in one language.
 *
 * @param id the content's identifier
 * @param slug the number, as text
 * @param japaneseTitle the title as published in Japan, one for every language; null when
 * unknown
 * @param coverImageId the cover's identifier; null when the volume has none
 * @param chapters what its chapters online amount to
 * @param arcs the arcs of those chapters, by number; empty until added
 * @param releases its release day in every country it has one; empty until added
 * @param publishedAt when the online version was published
 */
@Builder
public record Volume(UUID id, String slug, int number, String language, String title, String japaneseTitle,
		String coverImageId, StorySpan chapters, @With List<ArcSummary> arcs, @With List<Release> releases,
		Instant publishedAt) implements PublishedContent {

	public Volume {
		arcs = arcs == null ? List.of() : List.copyOf(arcs);
		releases = releases == null ? List.of() : List.copyOf(releases);
	}

}
