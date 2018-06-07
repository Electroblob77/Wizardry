package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class Light extends Spell {

	public Light() {
		super(EnumTier.BASIC, 5, EnumElement.SORCERY, "light", EnumSpellType.UTILITY, 15, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(4, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
			
			int blockHitX = rayTrace.blockX;
			int blockHitY = rayTrace.blockY;
			int blockHitZ = rayTrace.blockZ;
			int blockHitSide = rayTrace.sideHit;
			
			switch(blockHitSide){
			case 0: blockHitY--; break;
			case 1: blockHitY++; break;
			case 2: blockHitZ--; break;
			case 3: blockHitZ++; break;
			case 4: blockHitX--; break;
			case 5: blockHitX++; break;
			}
			
			if(world.isAirBlock(blockHitX, blockHitY, blockHitZ)){
				
				if(!world.isRemote){
					world.setBlock(blockHitX, blockHitY, blockHitZ, Wizardry.magicLight);
					if(world.getTileEntity(blockHitX, blockHitY, blockHitZ) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(blockHitX, blockHitY, blockHitZ)).setLifetime((int)(600*durationMultiplier));
					}
				}
				
				caster.swingItem();
				world.playSoundAtEntity(caster, "wizardry:aura", 1.0f, 1.0f);
				return true;
			}
		}else{
			int x = (int) (Math.floor(caster.posX) + caster.getLookVec().xCoord*4);
			int y = (int) (Math.floor(caster.posY) + caster.eyeHeight + caster.getLookVec().yCoord*4);
			int z = (int) (Math.floor(caster.posZ) + caster.getLookVec().zCoord*4);
			
			if(world.isAirBlock(x, y, z)){
				//world.playSound(x, y, z, "sound.ambient.cave.cave", 1.0f, 1.5f, false);
				if(!world.isRemote){
					world.setBlock(x, y, z, Wizardry.magicLight);
					if(world.getTileEntity(x, y, z) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(x, y, z)).setLifetime((int)(600*durationMultiplier));
					}
				}
				caster.swingItem();
				world.playSoundAtEntity(caster, "wizardry:aura", 1.0f, 1.0f);
				return true;
			}
		}
		return false;
	}


}
