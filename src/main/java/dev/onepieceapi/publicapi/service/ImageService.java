package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Image;
import dev.onepieceapi.publicapi.persistence.PublishedImageRepository;
import dev.onepieceapi.publicapi.service.exception.ContentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/** The images of the contents online (plan D13). */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ImageService {

	/** An image's id is the SHA-256 of its bytes, in lowercase hex. */
	private static final Pattern ID_FORMAT = Pattern.compile("[0-9a-f]{64}");

	private final PublishedImageRepository repository;

	/** Any other id is answered without asking the database: nothing can be there. */
	public Image find(String id) {
		if (!ID_FORMAT.matcher(id).matches()) {
			throw new ContentNotFoundException(id);
		}
		return this.repository.findById(id).orElseThrow(() -> new ContentNotFoundException(id));
	}

}
