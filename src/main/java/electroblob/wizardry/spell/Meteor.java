package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.EntityMeteor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class Meteor extends Spell {

	public Meteor() {
		super(EnumTier.MASTER, 100, EnumElement.FIRE, "meteor", EnumSpellType.ATTACK, 200, EnumAction.none, false);
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
					EntityMeteor meteor = new EntityMeteor(world, x, y + 50, z, Blocks.stone, blastMultiplier);
					world.spawnEntityInWorld(meteor);
				}

				caster.swingItem();
				world.playSoundAtEntity(caster, "wizardry:darkaura", 3.0f, 1.0f);
				return true;
        	}
		}
		return false;
	}


}
