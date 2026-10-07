package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * What every content online has in common, whatever its entity: the identifier and the
 * address that names it (null when its romaji leaves none).
 */
public interface PublishedContent {

	UUID id();

	String slug();

}
