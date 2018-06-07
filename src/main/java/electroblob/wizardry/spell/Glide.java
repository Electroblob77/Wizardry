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

public class Glide extends Spell {

	public Glide() {
		super(EnumTier.ADVANCED, 5, EnumElement.EARTH, "glide", EnumSpellType.UTILITY, 0, EnumAction.none, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(caster.motionY < -0.1 && !caster.isInWater()){
			caster.motionY = -0.1;
			if(Math.abs(caster.motionX) < 0.4 && Math.abs(caster.motionZ) < 0.4){
				caster.addVelocity(caster.getLookVec().xCoord/8, 0, caster.getLookVec().zCoord/8);
				//entityplayer.moveEntity(entityplayer.motionX*10, 0, entityplayer.motionZ*10);
			}
			caster.fallDistance = 0.0f;
		}
		
		if(world.isRemote){
			Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX - 0.25d + world.rand.nextDouble()/2, WizardryUtilities.getPlayerEyesPos(caster) - 1.5f + world.rand.nextDouble(), caster.posZ - 0.25d + world.rand.nextDouble()/2, 0, -0.1F, 0, 15, 1.0f, 1.0f, 1.0f);
			Wizardry.proxy.spawnParticle(EnumParticleType.LEAF, world, caster.posX - 0.25d + world.rand.nextDouble()/2, WizardryUtilities.getPlayerEyesPos(caster) - 1.5f + world.rand.nextDouble(), caster.posZ - 0.25d + world.rand.nextDouble()/2, 0, -0.03, 0, 20);
		}
		
		//if(caster.getItemInUseDuration() == 0){
			//world.playSoundAtEntity(entityplayer, "wizardry:sparkle", 0.5F, 1.0f);
		//}
		
		if(ticksInUse % 24 == 0){
			world.playSoundAtEntity(caster, "mob.enderdragon.wings", 0.5F, 1.0f);
		}
		return true;
	}


}
