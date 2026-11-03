package dev.onepieceapi.publicapi.service;

import dev.onepieceapi.publicapi.domain.Character;
import dev.onepieceapi.publicapi.domain.CharacterImage;
import dev.onepieceapi.publicapi.domain.CharacterSummary;
import dev.onepieceapi.publicapi.domain.Page;
import dev.onepieceapi.publicapi.domain.Relative;
import dev.onepieceapi.publicapi.persistence.PublishedCharacterListRepository;
import dev.onepieceapi.publicapi.persistence.PublishedContentRepository;
import dev.onepieceapi.publicapi.persistence.PublishedRevealRepository;
import dev.onepieceapi.publicapi.persistence.PublishedViews;
import dev.onepieceapi.readertime.Gallery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The characters online: the generic rules, bound to their view, and the detail completed
 * with its epithets, aliases, species, memberships, Devil Fruits, relatives and bounties
 * (Character plan C2, C6, C8, C10, C12).
 */
@Service
public class CharacterService extends PublishedContentService<Character, CharacterSummary> {

	private final PublishedCharacterListRepository lists;

	private final PublishedRevealRepository reveals;

	CharacterService(LanguageService languages, PublishedContentRepository repository, ReaderSightService sights,
			PublishedCharacterListRepository lists, PublishedRevealRepository reveals) {
		super(languages, repository, sights, PublishedViews.CHARACTER);
		this.lists = lists;
		this.reveals = reveals;
	}

	/**
	 * Each row with its main portrait, from the galleries of the page in one read
	 * (workflow 3.5.1).
	 */
	@Override
	protected Page<CharacterSummary> completeRows(Page<CharacterSummary> page, String language) {
		Map<UUID, List<CharacterImage>> galleries = this.lists
			.findGalleries(page.content().stream().map(CharacterSummary::id).toList(), language);
		return page
			.map((summary) -> summary.withImageId(mainPortraitOf(galleries.getOrDefault(summary.id(), List.of()))));
	}

	@Override
	protected Character complete(Character character) {
		return character.withEpithets(this.lists.findEpithets(character.id(), character.language()))
			.withAliases(this.lists.findAliases(character.id()))
			.withSpecies(this.lists.findSpecies(character.id(), character.language()))
			.withAffiliations(this.lists.findAffiliations(character.id(), character.language()))
			.withDevilFruits(this.lists.findDevilFruits(character.id(), character.language()))
			.withRelatives(relativesOf(character))
			.withBounties(this.lists.findBounties(character.id(), character.language()))
			.withGallery(this.lists.findGallery(character.id(), character.language()))
			.withReveals(this.reveals.findCharacterReveals(character.id(), character.language()));
	}

	private static String mainPortraitOf(List<CharacterImage> gallery) {
		return Gallery.mainPortrait(gallery).map(CharacterImage::imageId).orElse(null);
	}

	/**
	 * The relatives it stores, then the characters online that store it, each through the
	 * inverse kind (Character plan D13).
	 */
	private List<Relative> relativesOf(Character character) {
		Stream<Relative> stored = this.lists.findRelatives(character.id(), character.language()).stream();
		Stream<Relative> derived = this.lists.findNamingAsRelative(character.id(), character.language())
			.stream()
			.map(Relative::inverted);
		return Stream.concat(stored, derived).toList();
	}

}
