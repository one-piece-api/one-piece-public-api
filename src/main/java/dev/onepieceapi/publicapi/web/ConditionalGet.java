package dev.onepieceapi.publicapi.web;

import dev.onepieceapi.publicapi.domain.Snapshot;
import dev.onepieceapi.publicapi.service.PublishedSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Answers a read as a conditional GET (plan D13): the ETag is the published revision,
 * weak, so a caller who already holds it gets a {@code 304} without the data being read.
 * The comparison with {@code If-None-Match} (lists, {@code *}, weak tags) is the
 * framework's own, {@link WebRequest#checkNotModified(String)}.
 */
@Component
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ConditionalGet {

	private final PublishedSnapshotService snapshots;

	/**
	 * @param query reads the data; not run when the caller is current
	 * @param response builds the answer from what was read and the ETag to put on it
	 */
	public <T, R> ResponseEntity<R> get(WebRequest request, Supplier<T> query,
			BiFunction<T, String, ResponseEntity<R>> response) {
		Snapshot<T> snapshot = this.snapshots.read(revision -> request.checkNotModified(entityTag(revision)), query);
		return switch (snapshot) {
			case Snapshot.NotModified<T> notModified ->
				ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(entityTag(notModified.revision())).build();
			case Snapshot.Fresh<T> fresh -> response.apply(fresh.value(), entityTag(fresh.revision()));
		};
	}

	/** Weak: the same revision can be served in different encodings. */
	private static String entityTag(long revision) {
		return "W/\"" + revision + "\"";
	}

}
