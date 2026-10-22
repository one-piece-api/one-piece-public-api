package dev.onepieceapi.publicapi.web.dto.request;

import dev.onepieceapi.publicapi.domain.AffiliationKind;
import dev.onepieceapi.publicapi.domain.CharacterStatus;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.Map;
import java.util.Optional;

/**
 * The filters of the character list (Character plan D16), all optional and combinable.
 *
 * @param status only the characters of this status
 * @param species a species of their list, by id or slug
 * @param affiliation an affiliation they belong to now, by id or slug
 * @param affiliationKind the kind of an affiliation they belong to now
 * @param devilFruit a Devil Fruit they have, now or once, by id or slug
 */
public record CharacterFilterRequest(
		@Parameter(description = "Only the characters of this status") CharacterStatus status,
		@Parameter(description = "Only the characters of this species, by id or slug (current or old); an unknown"
				+ " species lists nothing") String species,
		@Parameter(description = "Only the characters that belong to this affiliation now, by id or slug (current or"
				+ " old); those who left it do not count; an unknown affiliation lists nothing") String affiliation,
		@Parameter(
				description = "Only the characters that belong now to an affiliation of this kind") AffiliationKind affiliationKind,
		@Parameter(description = "Only the characters that have this Devil Fruit, now or once, by id or slug (current"
				+ " or old); an unknown fruit lists nothing") String devilFruit) {

	/** As the list reads them: by the name of the filter. */
	public Map<String, Optional<String>> asked() {
		return Map.of(PublishedViews.STATUS_FILTER, Optional.ofNullable(this.status).map(CharacterStatus::name),
				PublishedViews.SPECIES_FILTER, Optional.ofNullable(this.species), PublishedViews.AFFILIATION_FILTER,
				Optional.ofNullable(this.affiliation), PublishedViews.AFFILIATION_KIND_FILTER,
				Optional.ofNullable(this.affiliationKind).map(AffiliationKind::name), PublishedViews.DEVIL_FRUIT_FILTER,
				Optional.ofNullable(this.devilFruit));
	}

}
