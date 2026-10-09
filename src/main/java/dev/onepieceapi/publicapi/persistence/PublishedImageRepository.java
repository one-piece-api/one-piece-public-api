package dev.onepieceapi.publicapi.persistence;

import dev.onepieceapi.publicapi.domain.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** The images of the contents online, read through {@code published.image} (plan D13). */
@Repository
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class PublishedImageRepository {

	private final JdbcClient jdbc;

	public Optional<Image> findById(String id) {
		return this.jdbc.sql("SELECT content_type, bytes FROM published.image WHERE id = :id")
			.param("id", id)
			.query((resultSet, rowNumber) -> new Image(resultSet.getString("content_type"),
					resultSet.getBytes("bytes")))
			.optional();
	}

}
