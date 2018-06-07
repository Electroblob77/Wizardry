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

public class Leap extends Spell {

	public Leap() {
		super(EnumTier.BASIC, 10, EnumElement.EARTH, "leap", EnumSpellType.UTILITY, 20, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(caster.onGround){
			
			caster.motionY = 0.65 * damageMultiplier;
			caster.addVelocity(caster.getLookVec().xCoord*0.3, 0, caster.getLookVec().zCoord*0.3);
			
			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x = (double)(caster.posX + world.rand.nextFloat() - 0.5F);
					double y = (double)(caster.boundingBox.minY);
					double z = (double)(caster.posZ + world.rand.nextFloat() - 0.5F);
					world.spawnParticle("cloud", x, y, z, 0, 0, 0);
				}
			}
			
			world.playSoundAtEntity(caster, "mob.enderdragon.wings", 0.5F, 1.0f);
			caster.swingItem();
			return true;
		}
		
		return false;
	}


}
