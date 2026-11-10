package electroblob.wizardry.util;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class which (re)assigns every spell's networkID from scratch, once all spells (wizardry's own and any
 * spell packs') have finished registering. This is necessary because {@link Spell}'s constructor assigns IDs from a
 * simple incrementing counter in construction order, which only lines up consistently between client and server if
 * every mod's spells are constructed in exactly the same order on both sides - something that isn't guaranteed,
 * especially for spell packs with (load-after) dependencies.
 * <p></p>
 * Instead, IDs are assigned centrally here, from the registry names alone: the {@code none} spell is always 0,
 * wizardry's own spells occupy {@code 1..N} (sorted alphabetically by registry name), and every other mod's spells
 * follow, also sorted alphabetically by registry name. Because this depends only on registry names - which are
 * fixed, and must already be unique within the registry - rather than on construction order, it's deterministic
 * regardless of mod/classloading order, so there's no separate "assume this worked, then recover if it didn't"
 * fallback path needed any more; {@link #validateNetworkIDs()} is kept only as a cheap sanity check.
 */
public class SpellNetworkIDSorter {

	public static void init() {

		Wizardry.logger.debug("Assigning all spell networkIDs centrally, based on addon modid/registry name alphabetical order");

		List<Spell> ownSpells = Spell.getAllSpells().stream()
				.filter(spell -> spell.getRegistryName().getNamespace().equals(Wizardry.MODID))
				.sorted(Comparator.comparing(s -> s.getRegistryName().toString()))
				.collect(Collectors.toList());

		List<Spell> addonSpells = Spell.getAllSpells().stream()
				.filter(spell -> !spell.getRegistryName().getNamespace().equals(Wizardry.MODID))
				.sorted(Comparator.comparing(s -> s.getRegistryName().toString()))
				.collect(Collectors.toList());

		Spells.none.setId(0);

		int nextSpellId = 1;

		for(Spell spell : ownSpells){
			Wizardry.logger.debug("Updating networkID of spell " + spell.getRegistryName().toString() + " from " + spell.networkID() + " to " + nextSpellId);
			spell.setId(nextSpellId++);
		}

		for(Spell spell : addonSpells){
			Wizardry.logger.debug("Updating networkID of spell " + spell.getRegistryName().toString() + " from " + spell.networkID() + " to " + nextSpellId);
			spell.setId(nextSpellId++);
		}

		validateNetworkIDs();
	}

	/**
	 * Sanity-checks that every registered spell (including {@code none}) ended up with a unique networkID, and that
	 * together they form an unbroken {@code 0..(count-1)} range, exactly as {@link Spell#byNetworkID(int)} assumes.
	 * <p></p>
	 * This should always be true after the centralised assignment above, since it derives every ID directly from a
	 * simple partition and sort rather than from anything that can be inconsistent between client and server - the
	 * only way it could fail is a bug in that logic, or two spells somehow sharing a registry name (which the
	 * registry itself should already prevent). It's kept as a safety net purely to make such a bug loud and obvious
	 * rather than silently corrupting every packet that identifies a spell by its networkID.
	 */
	private static void validateNetworkIDs(){

		List<Spell> allSpells = new ArrayList<>(Spell.registry.getValuesCollection());

		Map<Integer, List<Spell>> byId = allSpells.stream().collect(Collectors.groupingBy(Spell::networkID));

		List<Integer> duplicateIds = byId.entrySet().stream().filter(e -> e.getValue().size() > 1)
				.map(Map.Entry::getKey).sorted().collect(Collectors.toList());

		List<Integer> missingIds = new ArrayList<>();
		for(int id = 0; id < allSpells.size(); id++) if(!byId.containsKey(id)) missingIds.add(id);

		if(!duplicateIds.isEmpty() || !missingIds.isEmpty()){

			Wizardry.logger.error("Spell networkIDs are inconsistent after the centralised assignment above! This" +
					" should not be possible, and if left alone WILL corrupt spell casting and property syncing" +
					" between client and server - and not just for the spell(s) named below, but potentially for" +
					" others too. This points to a bug in SpellNetworkIDSorter itself, or two spells somehow" +
					" sharing a registry name.");

			for(int id : duplicateIds){
				Wizardry.logger.error("networkID " + id + " is shared by " + byId.get(id).size() + " spells: "
						+ byId.get(id).stream().map(s -> s.getRegistryName().toString()).collect(Collectors.joining(", ")));
			}

			if(!missingIds.isEmpty()){
				Wizardry.logger.error(missingIds.size() + " networkID slot(s) ended up unused: " + missingIds);
			}
		}
	}
}
