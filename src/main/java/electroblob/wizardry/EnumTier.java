package electroblob.wizardry;

import net.minecraft.util.StatCollector;

public enum EnumTier {
	
	BASIC(700, 3, "\u00A7f", "basic"),
	APPRENTICE(1000, 4, "\u00A7b", "apprentice"),
	ADVANCED(1500, 5, "\u00A71", "advanced"),
	MASTER(2500, 6, "\u00A75", "master");
	
	/** Maximum mana a wand of this tier can store. */
	public final int maxCharge;
	/** Just an ordinal. Shouldn't really be needed but no point changing it now. */
	public final int level;
	/** The maximum number of upgrades that can be applied to a wand of this tier. */
	public final int upgradeLimit;
	/** The colour of text associated with this tier */
	public final String colour;
	
	private final String unlocalisedName;
	
	private EnumTier(int maxCharge, int upgradeLimit, String colour, String name){
		this.maxCharge = maxCharge;
		this.level = ordinal();
		this.upgradeLimit = upgradeLimit;
		this.colour = colour;
		this.unlocalisedName = name;
	}

	public String getDisplayName() {
		return StatCollector.translateToLocal("tier." + unlocalisedName);
	}
	
	public String getDisplayNameWithFormatting() {
		return this.colour + StatCollector.translateToLocal("tier." + unlocalisedName);
	}
}
