package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class Snare extends Spell {

	public Snare() {
		super(EnumTier.BASIC, 10, EnumElement.EARTH, "snare", EnumSpellType.ATTACK, 10, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(10*rangeMultiplier, world, caster, true);

		// Gets block the player is looking at and places snare
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){

			int blockHitX = rayTrace.blockX;
			int blockHitY = rayTrace.blockY;
			int blockHitZ = rayTrace.blockZ;
			int blockHitSide = rayTrace.sideHit;

			if(blockHitSide == 1 && world.isSideSolid(blockHitX, blockHitY, blockHitZ, ForgeDirection.UP) && WizardryUtilities.canBlockBeReplaced(world, blockHitX, blockHitY+1, blockHitZ)){

				if(!world.isRemote){
					world.setBlock(blockHitX, blockHitY+1, blockHitZ, Wizardry.snare);
					((TileEntityPlayerSave)world.getTileEntity(blockHitX, blockHitY+1, blockHitZ)).setCaster(caster);
				}

				double dx = blockHitX + 0.5 - caster.posX;
				double dy = blockHitY + 1.5 - (caster.posY + caster.height/2);
				double dz = blockHitZ + 0.5 - caster.posZ;

				if(world.isRemote){
					for(int i=1;i<5;i++){
						float brightness = world.rand.nextFloat()/4;
						Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0.0d, 0.0d, 0.0d, 20 + world.rand.nextInt(8), brightness, brightness + 0.1f, 0.0f);
						Wizardry.proxy.spawnParticle(EnumParticleType.LEAF, world, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, -0.01, 0, 40 + world.rand.nextInt(10));
					}
				}
				caster.swingItem();
				world.playSoundAtEntity(caster, "dig.grass", 1.0F, world.rand.nextFloat() * 0.4F + 1.2F);
				return true;
			}
		}
		return false;
	}


}
