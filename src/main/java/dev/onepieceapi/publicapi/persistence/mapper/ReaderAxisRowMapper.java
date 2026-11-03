package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.domain.LongTexts;
import dev.onepieceapi.publicapi.domain.Paragraph;
import dev.onepieceapi.publicapi.persistence.row.MeetingRow;
import dev.onepieceapi.publicapi.persistence.row.ParagraphRow;
import dev.onepieceapi.publicapi.persistence.row.WindowedRow;
import dev.onepieceapi.readertime.Point;
import dev.onepieceapi.readertime.RevealWindow;
import lombok.experimental.UtilityClass;

import java.util.List;

/** From the rows of the reader axis to the points of the library. */
@UtilityClass
public class ReaderAxisRowMapper {

	private static final String DESCRIPTION = "DESCRIPTION";

	private static final String ADVANTAGES = "ADVANTAGES";

	private static final String DISADVANTAGES = "DISADVANTAGES";

	public Point toPoint(MeetingRow row) {
		return new Point(row.chapter(), row.episode());
	}

	public RevealWindow toWindow(WindowedRow row) {
		return new RevealWindow(new Point(row.discoveredChapter(), row.discoveredEpisode()),
				new Point(row.endedChapter(), row.endedEpisode()));
	}

	/** The paragraphs of a content, rows ordered by section and position. */
	public LongTexts toLongTexts(List<ParagraphRow> rows) {
		return new LongTexts(section(rows, DESCRIPTION), section(rows, ADVANTAGES), section(rows, DISADVANTAGES));
	}

	private List<Paragraph> section(List<ParagraphRow> rows, String section) {
		return rows.stream()
			.filter((row) -> section.equals(row.section()))
			.map((row) -> new Paragraph(row.text(), toWindow(row)))
			.toList();
	}

}
