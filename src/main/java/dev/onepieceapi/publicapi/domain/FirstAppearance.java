package dev.onepieceapi.publicapi.domain;

/**
 * Where the reader first meets a content (plan of the timeline, T7): one part per medium,
 * only the manga's (the chapter) today; the episode is an open door.
 */
public record FirstAppearance(ChapterReference chapter) {

}
