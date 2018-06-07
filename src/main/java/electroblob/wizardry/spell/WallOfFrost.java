package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class WallOfFrost extends Spell {

	public WallOfFrost() {
		super(EnumTier.MASTER, 15, EnumElement.ICE, "wall_of_frost", EnumSpellType.UTILITY, 0, EnumAction.none, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		Vec3 look = caster.getLookVec();
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(10*rangeMultiplier, world, caster, true);
		
		if(rayTrace != null && !world.isRemote){
			int x = rayTrace.blockX;
			int y = rayTrace.blockY;
			int z = rayTrace.blockZ;
			
			// Stops the ice being placed floating above snow and grass. Directions other than up included for completeness.
			if(WizardryUtilities.canBlockBeReplaced(world, x, y, z)){
				switch(rayTrace.sideHit){
				case 0: y++; break;
				case 1: y--; break;
				case 2: z++; break;
				case 3: z--; break;
				case 4: x++; break;
				case 5: x--; break;
				}
			}
			
			if(caster.getDistance(x, y, z) > 2 && world.getBlock(x, y, z) != Wizardry.iceStatue){
			
				switch(rayTrace.sideHit){
				case 0:
					break;
				case 1:
					if(WizardryUtilities.canBlockBeReplaced(world, x, y+1, z)){
						world.setBlock(x, y+1, z, Wizardry.iceStatue);
					}
					if(WizardryUtilities.canBlockBeReplaced(world, x, y+2, z)){
						world.setBlock(x, y+2, z, Wizardry.iceStatue);
					}
					break;
				case 2:
					if(WizardryUtilities.canBlockBeReplaced(world, x, y, z-1)){
						world.setBlock(x, y, z-1, Wizardry.iceStatue);
					}
					break;
				case 3:
					if(WizardryUtilities.canBlockBeReplaced(world, x, y, z+1)){
						world.setBlock(x, y, z+1, Wizardry.iceStatue);
					}
					break;
				case 4:
					if(WizardryUtilities.canBlockBeReplaced(world, x-1, y, z)){
						world.setBlock(x-1, y, z, Wizardry.iceStatue);
					}
					break;
				case 5:
					if(WizardryUtilities.canBlockBeReplaced(world, x+1, y, z)){
						world.setBlock(x+1, y, z, Wizardry.iceStatue);
					}
					break;
				}
			}
		}
		for(int i=0; i<20; i++){
			if(world.isRemote){
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, look.xCoord*rangeMultiplier, look.yCoord*rangeMultiplier, look.zCoord*rangeMultiplier, 8 + world.rand.nextInt(12), 0.4f, 0.6f, 1.0f);
				
				x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, look.xCoord*rangeMultiplier, look.yCoord*rangeMultiplier, look.zCoord*rangeMultiplier, 8 + world.rand.nextInt(12), 1.0f, 1.0f, 1.0f);
			}
		}
		if(ticksInUse % 12 == 0){
			if(ticksInUse == 0) world.playSoundAtEntity(caster, "wizardry:ice", 0.5F, 1.0f);
			world.playSoundAtEntity(caster, "wizardry:frostray", 0.5F, 1.0f);
		}
		return true;
	}


}
