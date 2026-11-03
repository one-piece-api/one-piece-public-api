package dev.onepieceapi.publicapi.domain;

/**
 * The revealable values of a Bounty (workflow 3.7.1).
 *
 * @param inactive whether the poster no longer holds; null when not held
 */
public record BountyValues(Boolean inactive) implements RevealedValues<BountyField, BountyValues> {

	/** Nothing known. */
	public static final BountyValues UNKNOWN = new BountyValues(null);

	@Override
	public boolean holds(BountyField field) {
		return this.inactive != null;
	}

	@Override
	public BountyValues withField(BountyField field, BountyValues other) {
		return other;
	}

}
