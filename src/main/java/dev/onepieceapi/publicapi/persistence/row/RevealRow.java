package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * What every row of an initial knowledge or a reveal holds, whatever the entity (workflow
 * 3.7.1); the initial knowledge has no fields, title or point.
 */
public interface RevealRow {

	UUID contentId();

	/** The fields a reveal names, comma separated. */
	String fields();

	String title();

	Integer revealedChapter();

	Integer revealedEpisode();

}
