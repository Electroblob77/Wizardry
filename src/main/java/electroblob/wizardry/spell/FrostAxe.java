package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class FrostAxe extends SpellConjuration {

	public FrostAxe(){
		super("frost_axe", WizardryItems.frost_axe);
	}

	@Override
	protected void spawnParticles(World world, EntityLivingBase caster, SpellModifiers modifiers){
		
		for(int i=0; i<10; i++){
			double x = caster.posX + world.rand.nextDouble() * 2 - 1;
			double y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
			double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
			ParticleBuilder.create(Type.SNOW).pos(x, y, z).spawn(world);
		}
	}

}
