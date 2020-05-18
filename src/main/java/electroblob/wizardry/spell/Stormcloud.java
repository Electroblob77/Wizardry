package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityStormcloud;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public class Stormcloud extends SpellConstructRanged<EntityStormcloud> {

	public Stormcloud(){
		super("stormcloud", EntityStormcloud::new, false);
		this.addProperties(DAMAGE, EFFECT_RADIUS);
		this.floor(true);
	}

	@Override
	protected void addConstructExtras(EntityStormcloud construct, EnumFacing side, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		construct.posY += 5;
	}
}
