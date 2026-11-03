package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * A bounty online and the one it replaces, read from {@code published.bounty}.
 *
 * @param previousId the bounty it replaces
 */
public record SuccessionRow(UUID id, UUID previousId) {

}
