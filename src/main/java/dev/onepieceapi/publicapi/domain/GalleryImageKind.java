package dev.onepieceapi.publicapi.domain;

import dev.onepieceapi.readertime.ImageKind;

/**
 * The kind of a gallery image in the public contract (workflow 3.5.1): the names
 * content-service stores, in English as every public value.
 */
public enum GalleryImageKind {

	/** How the character looks; may be the main portrait. */
	APPEARANCE,

	/** A transformation or a disguise; never the main portrait. */
	FORM;

	public static GalleryImageKind of(ImageKind kind) {
		return switch (kind) {
			case ASPETTO -> APPEARANCE;
			case FORMA -> FORM;
		};
	}

}
