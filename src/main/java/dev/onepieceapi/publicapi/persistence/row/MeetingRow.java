package dev.onepieceapi.publicapi.persistence.row;

import java.util.UUID;

/**
 * Where the reader meets a content online: the chapter and the episode numbers, each null
 * when not on that axis.
 */
public record MeetingRow(UUID id, Integer chapter, Integer episode) {

}
