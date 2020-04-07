package electroblob.wizardry.util;

import electroblob.wizardry.spell.Spell;

import java.util.Comparator;

/** Interface for things that have a list of spells that can be sorted using {@link SortType}. This allows
 * {@link electroblob.wizardry.client.gui.GuiButtonSpellSort GuiButtonSpellSort} to change its appearance based on the
 * current sort setting. */
public interface ISpellSortable {

	/** Returns the current sort type this {@code ISpellSortable} is set to sort by. */
	SortType getSortType();

	/** Returns true if this {@code ISpellSortable} is currently set to sort in descending order, false if it is
	 * set to sort in ascending order. */
	boolean isSortDescending();

	enum SortType {

		TIER("tier", Comparator.naturalOrder()),
		ELEMENT("element", Comparator.comparing(Spell::getElement).thenComparing(Spell::getTier)),
		ALPHABETICAL("alphabetical", Comparator.comparing(Spell::getUnlocalisedName));

		public String name;
		public Comparator<? super Spell> comparator;

		SortType(String name, Comparator<? super Spell> comparator){
			this.name = name;
			this.comparator = comparator;
		}

	}

}
