package electroblob.wizardry;

import java.util.HashMap;
import java.util.Set;

import electroblob.wizardry.spell.Spell;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Much like {@link net.minecraft.enchantment.EnchantmentHelper EnchantmentHelper}, this class has some static methods
 * which allow cleaner and more concise interaction with the wand NBT data, which is quite a complex structure. Such
 * interaction previously resulted in rather verbose and repetitive code which was hard to read and even harder to debug!
 * For example, this class allowed {@link electroblob.wizardry.item.ItemWand ItemWand} to be shortened by about 80 lines.
 * In addition, by having all the various null checks and array size checks in one place, the chance of accidental errors
 * due to forgetting to check these things is greatly reduced.
 * <p>
 * Note that these methods contain no game logic at all; they are purely for interacting with the NBT data. Conversely,
 * you should never need to access the wand's NBT data directly when using this class, but the keys are public in the
 * unlikely case that this is necessary.
 * <p>
 * Also note that none of the methods in this class actually check that the given ItemStack contains an ItemWand; you
 * can, for example, pass in a stack of snowballs without causing problems, but that is of course pointless!
 * <p>
 * All <b>get</b> methods in this class return some kind of default if the passed-in wand stack has no nbt data.<br>
 * All <b>set</b> methods in this class create a new nbt data for the passed-in wand if it has none, before doing 
 * whatever else they do.
 * @see electroblob.wizardry.item.ItemWand ItemWand
 * @see electroblob.wizardry.packet.PacketControlInput PacketControlInput
 * @since Wizardry 1.1 */
public class WandHelper {

	// NBT tag keys
	public static final String SPELL_ARRAY_KEY = "spells";
	public static final String SELECTED_SPELL_KEY = "selectedSpell";
	public static final String COOLDOWN_ARRAY_KEY = "cooldown";
	public static final String UPGRADES_KEY = "upgrades";

	private static final HashMap<Item, String> upgradeMap = new HashMap<Item, String>();

	static {
		upgradeMap.put(Wizardry.condenserUpgrade, "condenser");
		upgradeMap.put(Wizardry.storageUpgrade, "storage");
		upgradeMap.put(Wizardry.siphonUpgrade, "siphon");
		upgradeMap.put(Wizardry.rangeUpgrade, "range");
		upgradeMap.put(Wizardry.durationUpgrade, "duration");
		upgradeMap.put(Wizardry.cooldownUpgrade, "cooldown");
		upgradeMap.put(Wizardry.blastUpgrade, "blast");
		upgradeMap.put(Wizardry.attunementUpgrade, "attunement");
	}

	/** Returns an array containing the spells currently bound to the given wand. As of Wizardry 1.1, this array is not
	 * always the same size; it can be anywhere between 5 and 8 (inclusive) in length. If the wand has no spell data,
	 * returns an array of length 0. */
	public static Spell[] getSpells(ItemStack wand){

		Spell[] spells = new Spell[0];

		if(wand.stackTagCompound != null){

			int[] spellIDs = wand.stackTagCompound.getIntArray(SPELL_ARRAY_KEY);

			spells = new Spell[spellIDs.length];

			for(int i=0; i<spellIDs.length; i++){
				spells[i] = Spell.get(spellIDs[i]);
			}
		}

		return spells;
	}

	/** Binds the given array of spells to the given wand. The array can be anywhere between 5 and 8 (inclusive) in length. */
	public static void setSpells(ItemStack wand, Spell[] spells){

		if(wand.stackTagCompound == null) wand.stackTagCompound = new NBTTagCompound();

		int[] spellIDs = new int[spells.length];

		for(int i=0; i<spells.length; i++){
			spellIDs[i] = spells[i] != null ? spells[i].id() : WizardryRegistry.none.id();
		}

		wand.stackTagCompound.setIntArray(SPELL_ARRAY_KEY, spellIDs);
	}

	/** Returns the currently selected spell for the given wand, or the 'none' spell if the wand has no spell data. */
	public static Spell getCurrentSpell(ItemStack wand){

		Spell[] spells = getSpells(wand);

		if(wand.stackTagCompound != null){

			int selectedSpell = wand.stackTagCompound.getInteger(SELECTED_SPELL_KEY);

			if(selectedSpell < spells.length && selectedSpell >= 0){
				return spells[selectedSpell];
			}
		}

		return WizardryRegistry.none;
	}

	/** Selects the next spell in this wand's list of spells. */
	public static void selectNextSpell(ItemStack wand){
		// 5 here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
		if(getSpells(wand).length < 0) setSpells(wand, new Spell[5]);
		
		if(wand.stackTagCompound != null){

			int numberOfSpells = getSpells(wand).length;
			int selectedSpell = wand.stackTagCompound.getInteger(SELECTED_SPELL_KEY);
			
			// Greater than or equal to so that if attunement upgrades are somehow removed by NBT modification it just
			// resets.
			if(selectedSpell >= numberOfSpells - 1){
				selectedSpell = 0;
			}else{
				selectedSpell++;
			}
			
			wand.stackTagCompound.setInteger(SELECTED_SPELL_KEY, selectedSpell);
		
		}
	}
	
	/** Selects the previous spell in this wand's list of spells. */
	public static void selectPreviousSpell(ItemStack wand){
		
		// 5 here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
		if(getSpells(wand).length < 0) setSpells(wand, new Spell[5]);
		// This cannot possibly be null here, and yet I am getting an NPE...
		if(wand.stackTagCompound != null){
	
			int numberOfSpells = getSpells(wand).length;
			int selectedSpell = wand.stackTagCompound.getInteger(SELECTED_SPELL_KEY);
			
			if(selectedSpell <= 0){
				selectedSpell = numberOfSpells - 1;
			}else{
				selectedSpell--;
			}
			
			wand.stackTagCompound.setInteger(SELECTED_SPELL_KEY, selectedSpell);
		}
	}

	/** Returns an array of the cooldowns for each spell bound to the given wand. As of Wizardry 1.1, this array is not
	 * always the same size; it can be anywhere between 5 and 8 (inclusive) in length. If the wand has no cooldown data,
	 * returns an array of length 0. */
	public static int[] getCooldowns(ItemStack wand){

		int[] cooldowns = new int[0];

		if(wand.stackTagCompound != null){

			return wand.stackTagCompound.getIntArray(COOLDOWN_ARRAY_KEY);
		}

		return cooldowns;
	}

	/** Sets the given wand's cooldown array. The array can be anywhere between 5 and 8 (inclusive) in length. */
	public static void setCooldowns(ItemStack wand, int[] cooldowns){

		if(wand.stackTagCompound == null) wand.stackTagCompound = new NBTTagCompound();

		wand.stackTagCompound.setIntArray(COOLDOWN_ARRAY_KEY, cooldowns);
	}

	/** Decrements the cooldown for each spell bound to the given wand by 1, if that cooldown is greater than 0. */
	public static void decrementCooldowns(ItemStack wand){

		int[] cooldowns = getCooldowns(wand);

		// If there are no cooldowns, it is assumed that they are all zero and therefore nothing needs to be done.
		if(cooldowns.length == 0) return;

		for(int i=0; i<cooldowns.length; i++){
			if(cooldowns[i] > 0) cooldowns[i]--;
		}

		setCooldowns(wand, cooldowns);
	}

	/** Returns the given wand's cooldown for the currently selected spell, or 0 if the wand has no cooldown data. */
	public static int getCurrentCooldown(ItemStack wand){

		int[] cooldowns = getCooldowns(wand);

		if(cooldowns.length == 0) return 0;
		// Don't need to check if the tag compound is null since the above check is equivalent.
		return cooldowns[wand.stackTagCompound.getInteger(SELECTED_SPELL_KEY)];
	}

	/** Sets the given wand's cooldown for the currently selected spell. */
	public static void setCurrentCooldown(ItemStack wand, int cooldown){

		if(wand.stackTagCompound == null) wand.stackTagCompound = new NBTTagCompound();

		int[] cooldowns = getCooldowns(wand);

		// The length of the spells array must be greater than 0 since this method can only be called if a spell is
		// cast, which is impossible if there are no spells.
		if(cooldowns.length == 0) cooldowns = new int[getSpells(wand).length];

		cooldowns[wand.stackTagCompound.getInteger(SELECTED_SPELL_KEY)] = cooldown;

		setCooldowns(wand, cooldowns);
	}

	/** Returns the number of upgrades of the given type that have been applied to the given wand, or 0 if the wand has
	 * no upgrade data or the given item is not a valid wand upgrade. */
	public static int getUpgradeLevel(ItemStack wand, Item upgrade){

		String key = upgradeMap.get(upgrade);

		if(wand.stackTagCompound != null && wand.stackTagCompound.hasKey(UPGRADES_KEY) && key != null){
			return wand.stackTagCompound.getCompoundTag(UPGRADES_KEY).getInteger(key);
		}

		return 0;

	}

	/** Returns the total number of upgrades that have been applied to the given wand, or 0 if the wand has no upgrade
	 * data. */
	public static int getTotalUpgrades(ItemStack wand){

		int totalUpgrades = 0;

		for(Item item : upgradeMap.keySet()){
			totalUpgrades += getUpgradeLevel(wand, item);
		}

		return totalUpgrades;
	}

	/** Applies the given upgrade to the given wand, or in other words increases the level for that upgrade by 1. This
	 * does <b>not</b> account for the individual or total upgrade stack limits. */
	public static void applyUpgrade(ItemStack wand, Item upgrade){

		if(wand.stackTagCompound == null) wand.stackTagCompound = new NBTTagCompound();

		if(!wand.stackTagCompound.hasKey(UPGRADES_KEY)) wand.stackTagCompound.setTag(UPGRADES_KEY, new NBTTagCompound());

		NBTTagCompound upgrades = wand.stackTagCompound.getCompoundTag(UPGRADES_KEY);

		String key = upgradeMap.get(upgrade);

		if(key != null) upgrades.setInteger(key, upgrades.getInteger(key) + 1);

		wand.stackTagCompound.setTag(UPGRADES_KEY, upgrades);
	}
	
	/** Returns true if the given item is a valid special wand upgrade. */
	public static boolean isWandUpgrade(Item upgrade){
		return upgradeMap.containsKey(upgrade);
	}
	
	/** Returns a set of all the items which are valid special wand upgrades. */
	public static Set<Item> getSpecialUpgrades(){
		return WandHelper.upgradeMap.keySet();
	}
	
	/** Registers a special upgrade with wizardry. Not used in the base mod, but I've put it here to make it easy
	 * for add-ons to add new wand upgrades.
	 * @param upgrade The wand upgrade item
	 * @param identifier A unique string, used as a key for wand nbt tags
	 * @throws IllegalArgumentException if the passed in identifier is already used for another wand upgrade */
	public static void registerSpecialUpgrade(Item upgrade, String identifier){
		// Throwing an exception is the best thing to do here, since if a duplicate was allowed weird things would
		// happen later with wand NBT.
		if(upgradeMap.containsValue(identifier)) throw new IllegalArgumentException("Duplicate wand upgrade identifier: " + identifier);
		upgradeMap.put(upgrade, identifier);
	}
}
