package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.construct.EntityBlackHole;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class BlackHole extends Spell {

	public BlackHole() {
		super(EnumTier.MASTER, 150, EnumElement.SORCERY, "black_hole", EnumSpellType.ATTACK, 400, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(10*rangeMultiplier, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
			int blockHitX = rayTrace.blockX;
			int blockHitY = rayTrace.blockY;
			int blockHitZ = rayTrace.blockZ;
			//world.playSound(blockHitX, blockHitY, blockHitZ, "sound/ambient/cave/cave2", 1.0f, 1.5f, false);
			int blockHitSide = rayTrace.sideHit;
			boolean flag2 = false;
			switch(blockHitSide){
			case 0:
				if(world.isAirBlock(blockHitX, blockHitY-1, blockHitZ)){
					if(!world.isRemote){
						world.spawnEntityInWorld(new EntityBlackHole(world, (double)blockHitX+0.5, (double)blockHitY-1+0.5, (double)blockHitZ+0.5, caster, (int)(600*durationMultiplier), damageMultiplier));
					}
					flag2 = true;
				}
				break;
			case 1:
				if(world.isAirBlock(blockHitX, blockHitY+1, blockHitZ)){
					if(!world.isRemote){
						world.spawnEntityInWorld(new EntityBlackHole(world, (double)blockHitX+0.5, (double)blockHitY+1+0.5, (double)blockHitZ+0.5, caster, (int)(600*durationMultiplier), damageMultiplier));
					}
					flag2 = true;
				}
				break;
			case 2:
				if(world.isAirBlock(blockHitX, blockHitY, blockHitZ-1)){
					if(!world.isRemote){
						world.spawnEntityInWorld(new EntityBlackHole(world, (double)blockHitX+0.5, (double)blockHitY+0.5, (double)blockHitZ-1+0.5, caster, (int)(600*durationMultiplier), damageMultiplier));
					}
					flag2 = true;
				}
				break;
			case 3:
				if(world.isAirBlock(blockHitX, blockHitY, blockHitZ+1)){
					if(!world.isRemote){
						world.spawnEntityInWorld(new EntityBlackHole(world, (double)blockHitX+0.5, (double)blockHitY+0.5, (double)blockHitZ+1+0.5, caster, (int)(600*durationMultiplier), damageMultiplier));
					}
					flag2 = true;
				}
				break;
			case 4:
				if(world.isAirBlock(blockHitX-1, blockHitY, blockHitZ)){
					if(!world.isRemote){
						world.spawnEntityInWorld(new EntityBlackHole(world, (double)blockHitX-1+0.5, (double)blockHitY+0.5, (double)blockHitZ+0.5, caster, (int)(600*durationMultiplier), damageMultiplier));
					}
					flag2 = true;
				}
				break;
			case 5:
				if(world.isAirBlock(blockHitX+1, blockHitY, blockHitZ)){
					if(!world.isRemote){
						world.spawnEntityInWorld(new EntityBlackHole(world, (double)blockHitX+1, (double)blockHitY, (double)blockHitZ, caster, (int)(600*durationMultiplier), damageMultiplier));
					}
					flag2 = true;
				}
				break;
			}
			if(flag2){
				caster.swingItem();
				world.playSoundAtEntity(caster, "mob.wither.spawn", 2.0f, 0.7f);
				return true;
			}
		}else{
			int x = (int) (Math.floor(caster.posX) + caster.getLookVec().xCoord*8);
			int y = (int) (Math.floor(caster.posY) + caster.eyeHeight + caster.getLookVec().yCoord*8);
			int z = (int) (Math.floor(caster.posZ) + caster.getLookVec().zCoord*8);
			if(!world.isRemote){
				world.spawnEntityInWorld(new EntityBlackHole(world, x, y, z, caster, (int)(600*durationMultiplier), damageMultiplier));
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "mob.wither.spawn", 2.0f, 0.7f);
			return true;
		}
		return false;
	}


}
