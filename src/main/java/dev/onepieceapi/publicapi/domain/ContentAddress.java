package dev.onepieceapi.publicapi.domain;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/** How a content is addressed: by its id or by a slug (plan D4). */
@UtilityClass
public class ContentAddress {

	/** A UUID is recognised by its format; anything else is a slug. */
	private static final Pattern UUID_FORMAT = Pattern
		.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

	public boolean isId(String idOrSlug) {
		return UUID_FORMAT.matcher(idOrSlug).matches();
	}

}
