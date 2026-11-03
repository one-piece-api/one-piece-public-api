package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.Axis;
import dev.onepieceapi.readertime.Point;
import dev.onepieceapi.readertime.ReaderPosition;
import dev.onepieceapi.readertime.Visibility;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * What a reader at chapter or episode N sees of the contents online (workflow 3.8;
 * reveals plan D12, R7 D1): the reader's position and where the reader meets each
 * content, so the rules of the library {@code one-piece-reader-time} decide what is
 * visible.
 * <p>
 * A content without a meeting point (not online, or met on no axis) is never seen.
 */
public final class ReaderSight {

	private final ReaderPosition position;

	private final Map<UUID, Point> meetings;

	private ReaderSight(ReaderPosition position, Map<UUID, Point> meetings) {
		this.position = Objects.requireNonNull(position, "position");
		this.meetings = Map.copyOf(meetings);
	}

	/**
	 * @param position where the reader is
	 * @param meetings where the reader meets each content online: its first appearance,
	 * or its own number for a chapter or an episode
	 */
	public static ReaderSight of(ReaderPosition position, Map<UUID, Point> meetings) {
		return new ReaderSight(position, meetings);
	}

	public ReaderPosition position() {
		return this.position;
	}

	public Axis axis() {
		return this.position.axis();
	}

	/** Whether the reader has reached the point, on the reader's axis. */
	public boolean reached(Point point) {
		return this.position.hasReached(point);
	}

	/** Whether the reader has met the content. */
	public boolean sees(UUID id) {
		return id != null && Visibility.itemVisibleAt(this.meetings.get(id), this.position);
	}

	/** The link to a content, or null when the reader has not met it yet. */
	public <T> T ifSeen(T link, Function<T, UUID> id) {
		return (link != null && sees(id.apply(link))) ? link : null;
	}

	/** The entries pointing to a content the reader has met, in their order. */
	public <T> List<T> seen(List<T> entries, Function<T, UUID> id) {
		return entries.stream().filter((entry) -> sees(id.apply(entry))).toList();
	}

	/** The contents the reader has met, in their order, each as the reader sees it. */
	public <T extends SeenByReader<T>> List<T> shown(List<T> contents) {
		return contents.stream().filter((content) -> sees(content.id())).map(this::shownOne).toList();
	}

	/**
	 * Only the part on the reader's axis: the other axis is not shown (workflow 3.8); the
	 * part kept is reached, its content being seen.
	 */
	public FirstAppearance onAxis(FirstAppearance firstAppearance) {
		if (firstAppearance == null) {
			return null;
		}
		return (axis() == Axis.CHAPTER) ? FirstAppearance.of(firstAppearance.chapter(), null)
				: FirstAppearance.of(null, firstAppearance.episode());
	}

	/** What the chapters online of a parent amount to, up to the reader's position. */
	public StorySpan chapterSpan(List<StoryDay> chapters) {
		return StorySpan.of(chapters.stream().filter(this::reachedChapter).toList());
	}

	/** What the episodes online of a parent amount to, up to the reader's position. */
	public StorySpan episodeSpan(List<StoryDay> episodes) {
		return StorySpan.of(episodes.stream().filter(this::reachedEpisode).toList());
	}

	private boolean reachedChapter(StoryDay chapter) {
		return reached(Point.ofChapter(chapter.number()));
	}

	private boolean reachedEpisode(StoryDay episode) {
		return reached(Point.ofEpisode(episode.number()));
	}

	private <T extends SeenByReader<T>> T shownOne(T content) {
		return content.seenBy(this);
	}

}
