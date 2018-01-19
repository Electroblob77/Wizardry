package electroblob.wizardry.constants;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum SpellType {
	
	ATTACK("attack"),
	DEFENCE("defence"),
	UTILITY("utility"),
	MINION("minion");

	private final String unlocalisedName;
	
	SpellType(String name){
		this.unlocalisedName = name;
	}

	@SideOnly(Side.CLIENT)
	public String getDisplayName(){
		return net.minecraft.client.resources.I18n.format("spelltype." + unlocalisedName);
	}
}