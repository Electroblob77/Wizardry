package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class Telekinesis extends Spell {

	public Telekinesis() {
		super(EnumTier.BASIC, 5, EnumElement.SORCERY, "telekinesis", EnumSpellType.UTILITY, 5, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 8*rangeMultiplier, 3.0f);

		if(rayTrace != null && rayTrace.entityHit != null){

			if(rayTrace.entityHit instanceof EntityItem){

				Entity entityHit = rayTrace.entityHit;
				entityHit.motionX = (caster.posX - entityHit.posX)/6;
				entityHit.motionY = (caster.posY + caster.eyeHeight - entityHit.posY)/6;
				entityHit.motionZ = (caster.posZ - entityHit.posZ)/6;
				world.playSoundAtEntity(entityHit, "wizardry:aura", 1.0F, 1.0f);
				caster.swingItem();
				return true;

			}else if(rayTrace.entityHit instanceof EntityPlayer && Wizardry.telekineticDisarmament){

				EntityPlayer target = (EntityPlayer)rayTrace.entityHit;

				if(target.getHeldItem() != null){

					if(!world.isRemote){
						EntityItem item = target.entityDropItem(target.getHeldItem(), 0.0f);
						// Makes the item move towards the caster
						item.motionX = (caster.posX - target.posX)/20;
						item.motionZ = (caster.posZ - target.posZ)/20;
					}
					target.setCurrentItemOrArmor(0, null);

					world.playSoundAtEntity(target, "wizardry:aura", 1.0F, 1.0f);
					caster.swingItem();
					return true;
				}
			}
		}
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
			Block block = world.getBlock(rayTrace.blockX, rayTrace.blockY, rayTrace.blockZ);
			if(block.onBlockActivated(world, rayTrace.blockX, rayTrace.blockY, rayTrace.blockZ, caster, 0, 0, 0, 0)){
				world.playSound(rayTrace.blockX, rayTrace.blockY, rayTrace.blockZ, "wizardry:aura", 1.0F, 1.0f, false);
				caster.swingItem();
				return true;
			}
		}
		return false;
	}

	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){

		if(target instanceof EntityPlayer && target.getHeldItem() != null){

			if(!world.isRemote){
				EntityItem item = target.entityDropItem(target.getHeldItem(), 0.0f);
				// Makes the item move towards the caster
				item.motionX = (caster.posX - target.posX)/20;
				item.motionZ = (caster.posZ - target.posZ)/20;
			}
			target.setCurrentItemOrArmor(0, null);

			world.playSoundAtEntity(target, "wizardry:aura", 1.0F, 1.0f);
			caster.swingItem();
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
