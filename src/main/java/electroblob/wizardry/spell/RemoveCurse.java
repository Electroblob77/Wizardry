package electroblob.wizardry.spell;

import electroblob.wizardry.potion.Curse;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.ArrayList;

public class RemoveCurse extends SpellBuff {

	public RemoveCurse(){
		super("remove_curse", 1, 1, 0.3f);
		this.soundValues(0.7f, 1.2f, 0.4f);
	}
	
	@Override
	protected boolean applyEffects(EntityLivingBase caster, SpellModifiers modifiers){

		if(!caster.getActivePotionEffects().isEmpty()){

			boolean flag = false;

			for(PotionEffect effect : new ArrayList<>(caster.getActivePotionEffects())){ // Get outta here, CMEs
				// The PotionEffect version (as opposed to Potion) does not call cleanup callbacks
				if(effect.getPotion() instanceof Curse){
					caster.removePotionEffect(effect.getPotion());
					flag = true;
				}
			}

			return flag;
		}

		return false;
	}

	@Override
	protected void spawnParticles(World world, EntityLivingBase caster, SpellModifiers modifiers){

		super.spawnParticles(world, caster, modifiers);

		for(int i = 0; i < particleCount*2; i++){
			double x = caster.posX + world.rand.nextDouble() * 2 - 1;
			double y = caster.posY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
			double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.14, 0).clr(0x0f001b)
					.time(20 + world.rand.nextInt(12)).spawn(world);
			ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0x0f001b).spawn(world);
		}
	}
}
