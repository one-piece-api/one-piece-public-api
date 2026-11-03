package dev.onepieceapi.publicapi.persistence.mapper;

import dev.onepieceapi.publicapi.persistence.row.MeetingRow;
import dev.onepieceapi.readertime.Point;
import lombok.experimental.UtilityClass;

/** From the rows of the reader axis to the points of the library. */
@UtilityClass
public class ReaderAxisRowMapper {

	public Point toPoint(MeetingRow row) {
		return new Point(row.chapter(), row.episode());
	}

}
