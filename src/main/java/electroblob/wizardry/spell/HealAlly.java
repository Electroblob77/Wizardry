package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.client.particle.EntitySparkleFX;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class HealAlly extends Spell {

	public HealAlly() {
		super(EnumTier.APPRENTICE, 10, EnumElement.HEALING, "heal_ally", EnumSpellType.DEFENCE, 20, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier, 8.0f);

		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){
			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;
			if(target.getHealth() < target.getMaxHealth()){
				target.heal((int)(5*damageMultiplier));
				
				if(world.isRemote){
					for(int i=0; i<10; i++){
						double d0 = (double)((float)target.posX + world.rand.nextFloat()*2 - 1.0F);
						// Apparently the client side spawns the particles 1 block higher than it should... hence the - 0.5F.
						double d1 = (double)((float)WizardryUtilities.getEntityFeetPos(target) + target.height - 0.5f + world.rand.nextFloat());
						double d2 = (double)((float)target.posZ + world.rand.nextFloat()*2 - 1.0F);
						Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, d0, d1, d2, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
					}
				}
				
				caster.swingItem();
				world.playSoundAtEntity(target, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
				return true;
			}
		}
		return false;
	}


}
