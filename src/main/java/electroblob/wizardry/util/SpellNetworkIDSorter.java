package electroblob.wizardry.util;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Spell;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class which ensures that all spellpack spell networkIDs are distributed in alphabetical order. Introducing this was necessary as mods (especially with
 * (load-after) dependencies were not always registering their spells in the exact same order. This is definitely
 */
public class SpellNetworkIDSorter {

	public static void init() {

		//reorder network IDs based on addon modids. This is necessary as modloading order doesn't seem to be guaranteed to match between various servers and clients
		Wizardry.logger.debug("Reordering all spell NetworkIDs based on addon modid alphabetical order");

		// the first available ID after all default spells
		int nextSpellId = (int) Spell.getAllSpells().stream().filter(spell -> spell.getRegistryName().getNamespace().equals(Wizardry.MODID)).count() + 1;
		List<Spell> addonSpells = Spell.getAllSpells().stream().filter(spell -> !spell.getRegistryName().getNamespace().equals(Wizardry.MODID)).collect(Collectors.toList());

		// sorting spells
		addonSpells.sort(Comparator.comparing((Spell o) -> o.getRegistryName().toString()));
		for (Spell spell : addonSpells) {
			Wizardry.logger.debug("Updating networkID of spell " + spell.getRegistryName().toString() + " from " + spell.networkID() + " to " + nextSpellId);
			spell.setId(nextSpellId++);
		}
	}
}
