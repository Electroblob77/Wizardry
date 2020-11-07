package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class FlamingAxe extends SpellConjuration {

	public FlamingAxe(){
		super("flaming_axe", WizardryItems.flaming_axe);
		addProperties(DAMAGE, BURN_DURATION);
	}
	
	@Override
	protected void spawnParticles(World world, EntityLivingBase caster, SpellModifiers modifiers){
		
		for(int i=0; i<10; i++){
			double x = caster.posX + world.rand.nextDouble() * 2 - 1;
			double y = caster.posY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
			double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
			world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
		}
	}

}
