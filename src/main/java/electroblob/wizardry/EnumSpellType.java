package electroblob.wizardry;

import net.minecraft.util.StatCollector;

public enum EnumSpellType {
	
	ATTACK("attack"),
	DEFENCE("defence"),
	UTILITY("utility"),
	MINION("minion");

	private final String unlocalisedName;
	
	EnumSpellType(String name){
		this.unlocalisedName = name;
	}

	public String getDisplayName() {
		return StatCollector.translateToLocal("spelltype." + unlocalisedName);
	}
}