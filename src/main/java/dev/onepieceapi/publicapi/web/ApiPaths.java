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

	public static final String DEVIL_FRUITS = V1 + "/{lang}/devil-fruits";

	public static final String DEVIL_FRUIT = DEVIL_FRUITS + "/{idOrSlug}";

	public static final String SAGAS = V1 + "/{lang}/sagas";

	public static final String SAGA = SAGAS + "/{idOrSlug}";

	public static final String ARCS = V1 + "/{lang}/arcs";

	public static final String ARC = ARCS + "/{idOrSlug}";

	public static final String VOLUMES = V1 + "/{lang}/volumes";

	public static final String VOLUME = VOLUMES + "/{idOrSlug}";

	public static final String CHAPTERS = V1 + "/{lang}/chapters";

	public static final String CHAPTER = CHAPTERS + "/{idOrSlug}";

	public static final String EPISODES = V1 + "/{lang}/episodes";

	public static final String EPISODE = EPISODES + "/{idOrSlug}";

	public static final String BOUNTIES = V1 + "/{lang}/bounties";

	public static final String BOUNTY = BOUNTIES + "/{idOrSlug}";

	public static final String AFFILIATIONS = V1 + "/{lang}/affiliations";

	public static final String AFFILIATION = AFFILIATIONS + "/{idOrSlug}";

	/** Singular and plural are the same word in English. */
	public static final String SPECIES = V1 + "/{lang}/species";

	public static final String SPECIES_ITEM = SPECIES + "/{idOrSlug}";

	public static final String CHARACTERS = V1 + "/{lang}/characters";

	public static final String CHARACTER = CHARACTERS + "/{idOrSlug}";

	/** No language: an image is the same in all of them (plan D11). */
	public static final String IMAGE = V1 + "/images/{id}.png";

	private ApiPaths() {
	}

}
