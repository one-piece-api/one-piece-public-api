package dev.onepieceapi.publicapi.web;

/**
 * Every path this service exposes, relative to the API root: the gateway strips its own
 * prefix before forwarding (plan D10), so these are the same wherever the API is mounted.
 * Plain constants and an explicit private constructor, not {@code @UtilityClass}: it
 * makes the fields non-constant, which annotation attributes reject.
 */
public final class ApiPaths {

	public static final String V1 = "/v1";

	public static final String LANGUAGES = V1 + "/languages";

	/** The language comes right after the version (plan D4). */
	public static final String DEVIL_FRUIT_TYPES = V1 + "/{lang}/devil-fruit-types";

	public static final String DEVIL_FRUIT_TYPE = DEVIL_FRUIT_TYPES + "/{idOrSlug}";

	private ApiPaths() {
	}

}
