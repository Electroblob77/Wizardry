package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class Cobwebs extends Spell {
	
	private static final int baseDuration = 400;

	public Cobwebs() {
		super(EnumTier.ADVANCED, 30, EnumElement.EARTH, "cobwebs", EnumSpellType.ATTACK, 70, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(12*rangeMultiplier, world, caster, true);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
			
			int blockHitX = rayTrace.blockX;
			int blockHitY = rayTrace.blockY;
			int blockHitZ = rayTrace.blockZ;
			int blockHitSide = rayTrace.sideHit;
			
			boolean flag = false;
			
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
					world.setBlock(blockHitX, blockHitY, blockHitZ, Wizardry.vanishingCobweb);
					if(world.getTileEntity(blockHitX, blockHitY, blockHitZ) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(blockHitX, blockHitY, blockHitZ)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(blockHitX+1, blockHitY, blockHitZ)){
				if(!world.isRemote){
					world.setBlock(blockHitX+1, blockHitY, blockHitZ, Wizardry.vanishingCobweb);
					if(world.getTileEntity(blockHitX+1, blockHitY, blockHitZ) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(blockHitX+1, blockHitY, blockHitZ)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(blockHitX-1, blockHitY, blockHitZ)){
				if(!world.isRemote){
					world.setBlock(blockHitX-1, blockHitY, blockHitZ, Wizardry.vanishingCobweb);
					if(world.getTileEntity(blockHitX-1, blockHitY, blockHitZ) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(blockHitX-1, blockHitY, blockHitZ)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(blockHitX, blockHitY+1, blockHitZ)){
				if(!world.isRemote){
					world.setBlock(blockHitX, blockHitY+1, blockHitZ, Wizardry.vanishingCobweb);
					if(world.getTileEntity(blockHitX, blockHitY+1, blockHitZ) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(blockHitX, blockHitY+1, blockHitZ)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(blockHitX, blockHitY-1, blockHitZ)){
				if(!world.isRemote){
					world.setBlock(blockHitX, blockHitY-1, blockHitZ, Wizardry.vanishingCobweb);
					if(world.getTileEntity(blockHitX, blockHitY-1, blockHitZ) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(blockHitX, blockHitY-1, blockHitZ)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(blockHitX, blockHitY, blockHitZ+1)){
				if(!world.isRemote){
					world.setBlock(blockHitX, blockHitY, blockHitZ+1, Wizardry.vanishingCobweb);
					if(world.getTileEntity(blockHitX, blockHitY, blockHitZ+1) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(blockHitX, blockHitY, blockHitZ+1)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(blockHitX, blockHitY, blockHitZ-1)){
				if(!world.isRemote){
					world.setBlock(blockHitX, blockHitY, blockHitZ-1, Wizardry.vanishingCobweb);
					if(world.getTileEntity(blockHitX, blockHitY, blockHitZ-1) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(blockHitX, blockHitY, blockHitZ-1)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
				
			if(flag){
				caster.swingItem();
				world.playSoundAtEntity(caster, "random.fizz", 1.0f, 1.0f);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(target != null){
			
			int x = MathHelper.floor_double(target.posX);
			int y = (int)target.boundingBox.minY;
			int z = MathHelper.floor_double(target.posZ);
			
			boolean flag = false;
			
			if(world.isAirBlock(x, y, z)){
				if(!world.isRemote){
					world.setBlock(x, y, z, Wizardry.vanishingCobweb);
					if(world.getTileEntity(x, y, z) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(x+1, y, z)){
				if(!world.isRemote){
					world.setBlock(x+1, y, z, Wizardry.vanishingCobweb);
					if(world.getTileEntity(x+1, y, z) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(x+1, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(x-1, y, z)){
				if(!world.isRemote){
					world.setBlock(x-1, y, z, Wizardry.vanishingCobweb);
					if(world.getTileEntity(x-1, y, z) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(x-1, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(x, y+1, z)){
				if(!world.isRemote){
					world.setBlock(x, y+1, z, Wizardry.vanishingCobweb);
					if(world.getTileEntity(x, y+1, z) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(x, y+1, z)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(x, y-1, z)){
				if(!world.isRemote){
					world.setBlock(x, y-1, z, Wizardry.vanishingCobweb);
					if(world.getTileEntity(x, y-1, z) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(x, y-1, z)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(x, y, z+1)){
				if(!world.isRemote){
					world.setBlock(x, y, z+1, Wizardry.vanishingCobweb);
					if(world.getTileEntity(x, y, z+1) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(x, y, z+1)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
			if(world.isAirBlock(x, y, z-1)){
				if(!world.isRemote){
					world.setBlock(x, y, z-1, Wizardry.vanishingCobweb);
					if(world.getTileEntity(x, y, z-1) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(x, y, z-1)).setLifetime((int)(baseDuration*durationMultiplier));
					}
				}
				flag = true;
			}
				
			if(flag){
				caster.swingItem();
				world.playSoundAtEntity(caster, "random.fizz", 1.0f, 1.0f);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
