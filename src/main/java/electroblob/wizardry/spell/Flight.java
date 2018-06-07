package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class Flight extends Spell {

	public Flight() {
		super(EnumTier.MASTER, 10, EnumElement.EARTH, "flight", EnumSpellType.UTILITY, 0, EnumAction.none, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!caster.isInWater()){
			// The division thingy checks if the look direction is the opposite way to the velocity. If this is the
			// case then the velocity should be added regardless of the player's current speed.
			if((Math.abs(caster.motionX) < 0.6 || caster.motionX/caster.getLookVec().xCoord < 0)
					&& (Math.abs(caster.motionZ) < 0.6 || caster.motionZ/caster.getLookVec().zCoord < 0)){
				caster.addVelocity(caster.getLookVec().xCoord/20, 0, caster.getLookVec().zCoord/20);
			}
			//y velocity is handled separately to stop the player from falling from the sky when they reach maximum horizontal speed.
			if(Math.abs(caster.motionY) < 0.6 || caster.motionY/caster.getLookVec().yCoord < 0){
				caster.motionY += caster.getLookVec().yCoord/20 + 0.075;
			}
			caster.fallDistance = 0.0f;
		}
		/*if(world.isRemote){
			double x = entityplayer.posX - 0.25d + world.rand.nextDouble()/2;
			double y = entityplayer.posY - 0.3f;
			double z = entityplayer.posZ - 0.25d + world.rand.nextDouble()/2;
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkleFX(world, x, y, z, (x - entityplayer.posX)/5 + entityplayer.motionX*0.75, 0.1F, (z - entityplayer.posZ)/5 + entityplayer.motionZ*0.75, null, 1.0f, 1.0f, 1.0f, 15));
		}*/
		if(world.isRemote){
			Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX - 1 + world.rand.nextDouble()*2, WizardryUtilities.getPlayerEyesPos(caster) - 0.5f + world.rand.nextDouble(), caster.posZ - 1 + world.rand.nextDouble()*2, 0, -0.1F, 0, 15, 0.8f, 1.0f, 0.5f);
			Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX - 1 + world.rand.nextDouble()*2, WizardryUtilities.getPlayerEyesPos(caster) - 0.5f + world.rand.nextDouble(), caster.posZ - 1 + world.rand.nextDouble()*2, 0, -0.1F, 0, 15, 1.0f, 1.0f, 1.0f);
			//Minecraft.getMinecraft().effectRenderer.addEffect(new EntityLeafFX(world, entityplayer.posX - 0.25d + world.rand.nextDouble()/2, entityplayer.posY - 1.5f + world.rand.nextDouble()*2, entityplayer.posZ - 0.25d + world.rand.nextDouble()/2, 0, -0.03, 0, 20));
		}
		if(ticksInUse % 24 == 0){
			world.playSoundAtEntity(caster, "mob.enderdragon.wings", 0.5F, 1.0f);
		}
		return true;
	}


}
