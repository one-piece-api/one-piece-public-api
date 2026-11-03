package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.Paragraphs;

import java.util.List;

/**
 * The long texts of a content as paragraphs (workflow 3.7.3), each section in its order.
 * A section the entity does not have is empty.
 */
public record LongTexts(List<Paragraph> description, List<Paragraph> advantages, List<Paragraph> disadvantages) {

	public static final LongTexts NONE = new LongTexts(List.of(), List.of(), List.of());

	public LongTexts {
		description = List.copyOf(description);
		advantages = List.copyOf(advantages);
		disadvantages = List.copyOf(disadvantages);
	}

	/** Without a filter: the paragraphs without an end, an ended one being superseded. */
	public LongTexts current() {
		return new LongTexts(Paragraphs.current(this.description), Paragraphs.current(this.advantages),
				Paragraphs.current(this.disadvantages));
	}

	/**
	 * At N: the paragraphs discovered and not ended, each discovered on the reader's axis
	 * only (workflow 3.7.3, 3.8).
	 */
	public LongTexts seenBy(ReaderSight sight) {
		return new LongTexts(sight.paragraphs(this.description), sight.paragraphs(this.advantages),
				sight.paragraphs(this.disadvantages));
	}

}
