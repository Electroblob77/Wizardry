package electroblob.wizardry;

import net.minecraft.util.StatCollector;

public enum EnumElement {
	
	MAGIC("\u00A77", "simple"),
	FIRE("\u00A74", "fire"),
	ICE("\u00A7b", "ice"),
	LIGHTNING("\u00A73", "lightning"),
	NECROMANCY("\u00A75", "necromancy"),
	EARTH("\u00A72", "earth"),
	SORCERY("\u00A7a", "sorcery"),
	HEALING("\u00A7e", "healing");
	
	/** Display colour for this element */
	public final String colour;
	/** Unlocalised name for this element */
	public final String unlocalisedName;
	
	private EnumElement(String colour, String name){
		this.colour = colour;
		this.unlocalisedName = name;
	}
	
	public EnumElement get(int id){
		return this.values()[id];
	}

	/** Returns the translated display name of this element, without formatting. */
	public String getDisplayName() {
		return StatCollector.translateToLocal("element." + unlocalisedName);
	}

	/** Returns the translated display name for wizards of this element, shown in the trading GUI */
	public String getWizardName() {
		return StatCollector.translateToLocal("element." + unlocalisedName + ".wizard");
	}
}
