package dev.onepieceapi.publicapi.domain;

/**
 * The spoiler filter (plan of the timeline, T8): "I have read up to this chapter",
 * inclusive. Without a number nothing is hidden: the full truth.
 *
 * @param number the last chapter read, 0 or more; null for no filter
 */
public record AsOfChapter(Integer number) {

	public static final AsOfChapter NONE = new AsOfChapter(null);

	/** Whether anything is hidden. */
	public boolean applies() {
		return this.number != null;
	}

}
