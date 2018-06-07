package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.construct.EntityHammer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class LightningHammer extends Spell {

	public LightningHammer() {
		super(EnumTier.MASTER, 100, EnumElement.LIGHTNING, "lightning_hammer", EnumSpellType.ATTACK, 300, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(40*rangeMultiplier, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){

			int x = rayTrace.blockX;
			int y = rayTrace.blockY;
			int z = rayTrace.blockZ;

        	// Not sure why it is +1 but it has to be to work properly.
        	if(world.canBlockSeeTheSky(x, y+1, z)){
        		
				if(!world.isRemote){
					
					EntityHammer hammer = new EntityHammer(world, x+0.5, y + 50, z+0.5, caster, (int)(600*durationMultiplier), damageMultiplier);
					
					hammer.motionX = 0;
					hammer.motionY = -2;
					hammer.motionZ = 0;
					
					world.spawnEntityInWorld(hammer);
				}
				
				caster.swingItem();
				world.playSoundAtEntity(caster, "wizardry:darkaura", 3.0f, 1.0f);
				return true;
        	}
		}
		return false;
	}


}
