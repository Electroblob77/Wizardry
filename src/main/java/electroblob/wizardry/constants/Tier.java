package electroblob.wizardry.constants;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.Random;

public enum Tier {

	NOVICE(700, 3, 12, new Style().setColor(TextFormatting.WHITE), "novice"),
	APPRENTICE(1000, 5, 5, new Style().setColor(TextFormatting.AQUA), "apprentice"),
	ADVANCED(1500, 7, 2, new Style().setColor(TextFormatting.DARK_BLUE), "advanced"),
	MASTER(2500, 9, 1, new Style().setColor(TextFormatting.DARK_PURPLE), "master");

	/** Maximum mana a wand of this tier can store. */
	public final int maxCharge;
	/** Just an ordinal. Shouldn't really be needed but no point changing it now. */
	public final int level;
	/** The maximum number of upgrades that can be applied to a wand of this tier. */
	public final int upgradeLimit;
	/** The weight given to this tier in the standard weighting. */
	public final int weight;
	/** The colour of text associated with this tier. */
	// Changed to a Style object for consistency.
	private final Style colour;

	private final String unlocalisedName;

	Tier(int maxCharge, int upgradeLimit, int weight, Style colour, String name){
		this.maxCharge = maxCharge;
		this.level = ordinal();
		this.upgradeLimit = upgradeLimit;
		this.weight = weight;
		this.colour = colour;
		this.unlocalisedName = name;
	}

	/** Returns the tier with the given name, or throws an {@link java.lang.IllegalArgumentException} if no such
	 * tier exists. */
	public static Tier fromName(String name){

		for(Tier tier : values()){
			if(tier.unlocalisedName.equals(name)) return tier;
		}

		throw new IllegalArgumentException("No such tier with unlocalised name: " + name);
	}

	/** Returns the tier above this one, or the same tier if this is the highest tier. */
	public Tier next(){
		return ordinal() + 1 < values().length ? values()[ordinal() + 1] : this;
	}

	/** Returns the tier below this one, or the same tier if this is the lowest tier. */
	public Tier previous(){
		return ordinal() > 0 ? values()[ordinal() - 1] : this;
	}

	/** Returns the translated display name of this tier, without formatting. */
	public String getDisplayName(){
		return Wizardry.proxy.translate("tier." + unlocalisedName);
	}

	/**
	 * Returns a {@code TextComponentTranslation} which will be translated to the display name of the tier, without
	 * formatting (i.e. not coloured).
	 */
	public TextComponentTranslation getNameForTranslation(){
		return new TextComponentTranslation("tier." + unlocalisedName);
	}

	/** Returns the translated display name of this tier, with formatting. */
	public String getDisplayNameWithFormatting(){
		return Wizardry.proxy.translate("tier." + unlocalisedName, this.colour);
	}

	/**
	 * Returns a {@code TextComponentTranslation} which will be translated to the display name of the tier, with
	 * formatting (i.e. coloured).
	 */
	public ITextComponent getNameForTranslationFormatted(){
		return new TextComponentTranslation("tier." + unlocalisedName).setStyle(this.colour);
	}

	public String getUnlocalisedName(){
		return unlocalisedName;
	}

	public String getFormattingCode(){
		return colour.getFormattingCode();
	}

	/** The progression required for a wand to be upgraded to this tier. */
	public int getProgression(){
		return Wizardry.settings.progressionRequirements[this.ordinal() - 1];
	}

	/**
	 * Returns a random tier based on the standard weighting. Currently, the standard weighting is: Basic (Novice) 60%,
	 * Apprentice 25%, Advanced 10%, Master 5%. If an array of tiers is given, it picks a tier from the array, with the
	 * same relative weights for each. For example, if the array contains APPRENTICE and MASTER, then the weighting will
	 * become: Apprentice 83.3%, Master 16.7%.
	 */
	public static Tier getWeightedRandomTier(Random random, Tier... tiers){

		if(tiers.length == 0) tiers = values();

		int totalWeight = 0;

		for(Tier tier : tiers) totalWeight += tier.weight;

		int randomiser = random.nextInt(totalWeight);
		int cumulativeWeight = 0;

		for(Tier tier : tiers){
			cumulativeWeight += tier.weight;
			if(randomiser < cumulativeWeight) return tier;
		}

		// This will never happen, but it might as well be a sensible result.
		return tiers[tiers.length - 1];
	}

}
