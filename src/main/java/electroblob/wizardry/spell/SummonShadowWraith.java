package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntityShadowWraith;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SummonShadowWraith extends SpellMinion<EntityShadowWraith> {

	public SummonShadowWraith(){
		super("summon_shadow_wraith", EntityShadowWraith::new);
		this.soundValues(1, 1.1f, 0.1f);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getDescription(){
		return "\u00A7k" + super.getDescription();
	}

}
