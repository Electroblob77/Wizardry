package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.construct.EntityBlizzard;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class Blizzard extends Spell {

	public Blizzard() {
		super(EnumTier.ADVANCED, 40, EnumElement.ICE, "blizzard", EnumSpellType.ATTACK, 100, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(20*rangeMultiplier, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
			if(!world.isRemote){
				double x = rayTrace.blockX;
				double y = rayTrace.blockY;
				double z = rayTrace.blockZ;
				EntityBlizzard blizzard = new EntityBlizzard(world, x, y+1.5, z, caster, (int)(600*durationMultiplier), damageMultiplier);
				world.spawnEntityInWorld(blizzard);
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, 1.0F);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			if(!world.isRemote){
				double x = target.posX;
				double y = target.posY;
				double z = target.posZ;
				EntityBlizzard blizzard = new EntityBlizzard(world, x, y+0.5, z, caster, (int)(600*durationMultiplier), damageMultiplier);
				world.spawnEntityInWorld(blizzard);
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, 1.0F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}
	
}
