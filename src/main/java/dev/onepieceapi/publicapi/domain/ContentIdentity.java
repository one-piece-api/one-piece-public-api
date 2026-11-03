package dev.onepieceapi.publicapi.domain;

import java.util.UUID;

/**
 * Who a content is, with no more: its identifier and its current address (null when its
 * romaji leaves none). Enough to resolve the value of a list filter.
 */
public record ContentIdentity(UUID id, String slug) {

}
