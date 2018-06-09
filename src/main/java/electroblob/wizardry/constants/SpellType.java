package electroblob.wizardry.constants;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum SpellType {

	ATTACK("attack"),
	DEFENCE("defence"),
	UTILITY("utility"),
	MINION("minion"),
	BUFF("buff"),
	CONSTRUCT("construct"),
	PROJECTILE("projectile"),
	ALTERATION("alteration");

	private final String unlocalisedName;

	SpellType(String name){
		this.unlocalisedName = name;
	}

	@SideOnly(Side.CLIENT)
	public String getDisplayName(){
		return I18n.format("spelltype." + unlocalisedName);
	}
}