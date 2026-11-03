package dev.onepieceapi.publicapi.web.dto.response;

/**
 * What the reader believes of a bounty at its first appearance (workflow 3.7.1).
 *
 * @param inactive whether the poster no longer holds
 */
public record BountyKnowledgeResponse(Boolean inactive) {

}
