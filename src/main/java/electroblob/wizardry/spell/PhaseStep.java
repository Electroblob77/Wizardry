package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class PhaseStep extends Spell {

	public PhaseStep() {
		super(EnumTier.ADVANCED, 35, EnumElement.SORCERY, "phase_step", EnumSpellType.UTILITY, 40, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		// Phase step does not gain range from range multiplier, instead it increases the thickness
		// of the wall you can teleport through.
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(5, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){

			int blockHitX = rayTrace.blockX;
			int blockHitY = rayTrace.blockY;
			int blockHitZ = rayTrace.blockZ;
			int blockHitSide = rayTrace.sideHit;

			int playerY = (int)caster.posY;

			// The maximum wall thickness as determined by the range multiplier. The + 0.5f is so that
			// weird float processing doesn't incorrectly round it down.
			int maxThickness = 1 + (int)((rangeMultiplier - 1)/Wizardry.RANGE_INCREASE_PER_LEVEL + 0.5f);

			// How far the player needs to teleport to get through the wall
			int teleportDistance = 0;

			// The following prevents the player from teleporting into blocks and suffocating
			checkforspace:
				switch(blockHitSide){
				case -1:
					return false;
				case 0:
					return false;
				case 1:
					return false;
				case 2:
					for(int i = 0; i <= maxThickness; i++){
						// Prevents the player from teleporting through unbreakable blocks, so they cannot cheat in other mods' mazes and dungeons.
						if((WizardryUtilities.isBlockUnbreakable(world, blockHitX, playerY, blockHitZ + i) || WizardryUtilities.isBlockUnbreakable(world, blockHitX, playerY + 1, blockHitZ + i)) && !Wizardry.teleportThroughUnbreakableBlocks) return false;

						if(!world.getBlock(blockHitX, playerY, blockHitZ + i).getMaterial().blocksMovement() && !world.getBlock(blockHitX, playerY + 1, blockHitZ + i).getMaterial().blocksMovement()){
							teleportDistance = i;
							break checkforspace;
						}
					}
					return false;
				case 3:
					for(int i = 0; i <= maxThickness; i++){
						// Prevents the player from teleporting through unbreakable blocks, so they cannot cheat in other mods' mazes and dungeons.
						if((WizardryUtilities.isBlockUnbreakable(world, blockHitX, playerY, blockHitZ - i) || WizardryUtilities.isBlockUnbreakable(world, blockHitX, playerY + 1, blockHitZ - i)) && !Wizardry.teleportThroughUnbreakableBlocks) return false;

						if(!world.getBlock(blockHitX, playerY, blockHitZ - i).getMaterial().blocksMovement() && !world.getBlock(blockHitX, playerY + 1, blockHitZ - i).getMaterial().blocksMovement()){
							teleportDistance = i;
							break checkforspace;
						}
					}
					return false;
				case 4:
					for(int i = 0; i <= maxThickness; i++){
						// Prevents the player from teleporting through unbreakable blocks, so they cannot cheat in other mods' mazes and dungeons.
						if((WizardryUtilities.isBlockUnbreakable(world, blockHitX + i, playerY, blockHitZ) || WizardryUtilities.isBlockUnbreakable(world, blockHitX + i, playerY + 1, blockHitZ)) && !Wizardry.teleportThroughUnbreakableBlocks) return false;

						if(!world.getBlock(blockHitX + i, playerY, blockHitZ).getMaterial().blocksMovement() && !world.getBlock(blockHitX + i, playerY + 1, blockHitZ).getMaterial().blocksMovement()){
							teleportDistance = i;
							break checkforspace;
						}
					}
					return false;
				case 5:
					for(int i = 0; i <= maxThickness; i++){
						// Prevents the player from teleporting through unbreakable blocks, so they cannot cheat in other mods' mazes and dungeons.
						if((WizardryUtilities.isBlockUnbreakable(world, blockHitX - i, playerY, blockHitZ) || WizardryUtilities.isBlockUnbreakable(world, blockHitX - i, playerY + 1, blockHitZ)) && !Wizardry.teleportThroughUnbreakableBlocks) return false;

						if(!world.getBlock(blockHitX - i, playerY, blockHitZ).getMaterial().blocksMovement() && !world.getBlock(blockHitX - i, playerY + 1, blockHitZ).getMaterial().blocksMovement()){
							teleportDistance = i;
							break checkforspace;
						}
					}
					return false;
				}
			
			if(!world.isRemote){
				switch(blockHitSide){
				case 2:
					caster.setPositionAndUpdate(blockHitX + 0.5, caster.posY, blockHitZ + 0.5 + teleportDistance);
					break;
				case 3:
					caster.setPositionAndUpdate(blockHitX + 0.5, caster.posY, blockHitZ + 0.5 - teleportDistance);
					break;
				case 4:
					caster.setPositionAndUpdate(blockHitX + 0.5 + teleportDistance, caster.posY, blockHitZ + 0.5);
					break;
				case 5:
					caster.setPositionAndUpdate(blockHitX + 0.5 - teleportDistance, caster.posY, blockHitZ + 0.5);
					break;
				}
			}
			world.playSoundAtEntity(caster, "mob.endermen.portal", 1.0F, 1.0f);
			caster.swingItem();
			return true;
		}
		
		// This is here because the conditions are false on the client for whatever reason. (see the Javadoc for cast()
		// for an explanation)
		if(world.isRemote){
			for(int i=0;i<10;i++){
				double dx1 = caster.posX;
				double dy1 = WizardryUtilities.getPlayerEyesPos(caster) - 1.5 + 2*world.rand.nextFloat();
				double dz1 = caster.posZ;
				world.spawnParticle("portal", dx1, dy1, dz1, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
			}
		}
		
		return false;
	}


}
