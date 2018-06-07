package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntityWizard;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class Blink extends Spell {

	public Blink() {
		super(EnumTier.APPRENTICE, 15, EnumElement.SORCERY, "blink", EnumSpellType.UTILITY, 25, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(25*rangeMultiplier, world, caster, false);
		
		// It's worth noting that on the client side, the cast() method only gets called if the server side
		// cast method succeeded, so you need not check any conditions for spawning particles.
		if(world.isRemote){
			for(int i=0;i<10;i++){
				double dx1 = caster.posX;
				double dy1 = WizardryUtilities.getPlayerEyesPos(caster) - 1.5 + 2*world.rand.nextFloat();
				double dz1 = caster.posZ;
				// For portal particles, velocity is not velocity but the offset where they start, then drift to
				// the actual position given.
				world.spawnParticle("portal", dx1, dy1, dz1, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
			}
		}
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
			
			int blockHitX = rayTrace.blockX;
			int blockHitY = rayTrace.blockY;
			int blockHitZ = rayTrace.blockZ;
			int blockHitSide = rayTrace.sideHit;
			
			// This means stuff like snow layers is ignored, meaning when on snow-covered ground the player does
			// not teleport 1 block above the ground.
			if(blockHitSide == 1 && !world.getBlock(blockHitX, blockHitY, blockHitZ).getMaterial().blocksMovement()){
				blockHitY--;
			}
			
			// The following prevents the player from teleporting into blocks and suffocating
			switch(blockHitSide){
			case -1:
				return false;
			case 0:
				return false;
			case 1:
				if(world.getBlock(blockHitX, blockHitY + 1, blockHitZ).getMaterial().blocksMovement() || world.getBlock(blockHitX, blockHitY + 2, blockHitZ).getMaterial().blocksMovement()){
					return false;
				}
				break;
			case 2:
				if(world.getBlock(blockHitX, blockHitY, blockHitZ - 1).getMaterial().blocksMovement() || world.getBlock(blockHitX, blockHitY + 1, blockHitZ - 1).getMaterial().blocksMovement()){
					return false;
				}
				break;
			case 3:
				if(world.getBlock(blockHitX, blockHitY, blockHitZ + 1).getMaterial().blocksMovement() || world.getBlock(blockHitX, blockHitY + 1, blockHitZ + 1).getMaterial().blocksMovement()){
					return false;
				}
				break;
			case 4:
				if(world.getBlock(blockHitX - 1, blockHitY, blockHitZ).getMaterial().blocksMovement() || world.getBlock(blockHitX - 1, blockHitY + 1, blockHitZ).getMaterial().blocksMovement()){
					return false;
				}
				break;
			case 5:
				if(world.getBlock(blockHitX + 1, blockHitY, blockHitZ).getMaterial().blocksMovement() || world.getBlock(blockHitX + 1, blockHitY + 1, blockHitZ).getMaterial().blocksMovement()){
					return false;
				}
				break;
			}

			// Plays before and after so it is heard from both positions
			world.playSoundAtEntity(caster, "mob.endermen.portal", 1.0F, 1.0f);
			
			if(!world.isRemote){
				switch(blockHitSide){
				case 1:
					caster.setPositionAndUpdate(blockHitX + 0.5, blockHitY + 1, blockHitZ + 0.5);
					break;
				case 2:
					caster.setPositionAndUpdate(blockHitX + 0.5, blockHitY, blockHitZ - 0.5);
					break;
				case 3:
					caster.setPositionAndUpdate(blockHitX + 0.5, blockHitY, blockHitZ + 1.5);
					break;
				case 4:
					caster.setPositionAndUpdate(blockHitX - 0.5, blockHitY, blockHitZ + 0.5);
					break;
				case 5:
					caster.setPositionAndUpdate(blockHitX + 1.5, blockHitY, blockHitZ + 0.5);
					break;
				}
			}
			world.playSoundAtEntity(caster, "mob.endermen.portal", 1.0F, 1.0f);
			caster.swingItem();
			return true;
		}
		
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){

		double angle = Math.atan2(target.posZ - caster.posZ, target.posX - caster.posX) + world.rand.nextDouble()*Math.PI;
		double radius = caster.getDistance(target.posX, target.boundingBox.minY, target.posZ) + world.rand.nextDouble()*3.0d;
		
		int x = MathHelper.floor_double(target.posX + Math.sin(angle)*radius);
		int z = MathHelper.floor_double(target.posZ - Math.cos(angle)*radius);
		int y = WizardryUtilities.getNearestFloorLevel(world, x, (int)caster.boundingBox.minY, z, (int)radius);
		
		// It's worth noting that on the client side, the cast() method only gets called if the server side
		// cast method succeeded, so you need not check any conditions for spawning particles.
		
		// For some reason, the wizard version spwans the particles where the wizard started
		if(world.isRemote){
			for(int i=0;i<10;i++){
				double dx1 = caster.posX;
				double dy1 = caster.boundingBox.minY + caster.height*world.rand.nextFloat();
				double dz1 = caster.posZ;
				world.spawnParticle("portal", dx1, dy1, dz1, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
			}
		}
		
		if(y > -1){
			
			// This means stuff like snow layers is ignored, meaning when on snow-covered ground the caster does
			// not teleport 1 block above the ground.
			if(!world.getBlock(x, y, z).getMaterial().blocksMovement()){
				y--;
			}
			
			if(world.getBlock(x, y + 1, z).getMaterial().blocksMovement() || world.getBlock(x, y + 2, z).getMaterial().blocksMovement()){
				return false;
			}

			// Plays before and after so it is heard from both positions
			world.playSoundAtEntity(caster, "mob.endermen.portal", 1.0F, 1.0f);
			
			if(!world.isRemote){
				caster.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
			}
			
			world.playSoundAtEntity(caster, "mob.endermen.portal", 1.0F, 1.0f);
			caster.swingItem();
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs() {
		return true;
	}
	
}
