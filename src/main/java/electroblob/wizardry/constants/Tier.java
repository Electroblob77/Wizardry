package electroblob.wizardry.constants;

import java.util.Random;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum Tier {

	BASIC(700, 3, 12, new Style().setColor(TextFormatting.WHITE), "basic"), APPRENTICE(1000, 4, 5,
			new Style().setColor(TextFormatting.AQUA), "apprentice"), ADVANCED(1500, 5, 2,
					new Style().setColor(TextFormatting.DARK_BLUE),
					"advanced"), MASTER(2500, 6, 1, new Style().setColor(TextFormatting.DARK_PURPLE), "master");

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

	private Tier(int maxCharge, int upgradeLimit, int weight, Style colour, String name){
		this.maxCharge = maxCharge;
		this.level = ordinal();
		this.upgradeLimit = upgradeLimit;
		this.weight = weight;
		this.colour = colour;
		this.unlocalisedName = name;
	}

	@SideOnly(Side.CLIENT)
	public String getDisplayName(){
		return net.minecraft.client.resources.I18n.format("tier." + unlocalisedName);
	}

	@SideOnly(Side.CLIENT)
	public String getDisplayNameWithFormatting(){
		return this.getFormattingCode() + net.minecraft.client.resources.I18n.format("tier." + unlocalisedName);
	}

	public String getUnlocalisedName(){
		return unlocalisedName;
	}

	public String getFormattingCode(){
		return colour.getFormattingCode();
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

		for(Tier tier : tiers)
			totalWeight += tier.weight;

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
