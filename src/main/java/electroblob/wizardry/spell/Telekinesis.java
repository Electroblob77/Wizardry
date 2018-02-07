package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Telekinesis extends Spell {

	public Telekinesis(){
		super(Tier.BASIC, 5, Element.SORCERY, "telekinesis", SpellType.UTILITY, 5, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				8 * modifiers.get(WizardryItems.range_upgrade), 3.0f);

		if(rayTrace != null && rayTrace.entityHit != null){

			if(rayTrace.entityHit instanceof EntityItem){

				Entity entityHit = rayTrace.entityHit;
				entityHit.motionX = (caster.posX - entityHit.posX) / 6;
				entityHit.motionY = (caster.posY + caster.eyeHeight - entityHit.posY) / 6;
				entityHit.motionZ = (caster.posZ - entityHit.posZ) / 6;
				entityHit.playSound(WizardrySounds.SPELL_CONJURATION, 1.0F, 1.0f);
				caster.swingArm(hand);
				return true;

			}else if(rayTrace.entityHit instanceof EntityPlayer && Wizardry.settings.telekineticDisarmament){

				EntityPlayer target = (EntityPlayer)rayTrace.entityHit;

				if(target.getHeldItemMainhand() != null){

					if(!world.isRemote){
						EntityItem item = target.entityDropItem(target.getHeldItemMainhand(), 0.0f);
						// Makes the item move towards the caster
						item.motionX = (caster.posX - target.posX) / 20;
						item.motionZ = (caster.posZ - target.posZ) / 20;
					}

					target.setHeldItem(EnumHand.MAIN_HAND, null);

					target.playSound(WizardrySounds.SPELL_CONJURATION, 1.0F, 1.0f);
					caster.swingArm(hand);
					return true;
				}
			}
		}

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			IBlockState blockstate = world.getBlockState(new BlockPos(rayTrace.getBlockPos()));

			if(blockstate.getBlock().onBlockActivated(world, rayTrace.getBlockPos(), blockstate, caster, hand,
					rayTrace.sideHit, 0, 0, 0)){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0F, 1.0f);
				caster.swingArm(hand);
				return true;
			}
		}
		return false;
	}

	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target instanceof EntityPlayer && target.getHeldItemMainhand() != null){

			// IDEA: Disarm the offhand if the mainhand is empty or otherwise harmless?

			if(!world.isRemote){
				EntityItem item = target.entityDropItem(target.getHeldItemMainhand(), 0.0f);
				// Makes the item move towards the caster
				item.motionX = (caster.posX - target.posX) / 20;
				item.motionZ = (caster.posZ - target.posZ) / 20;
			}

			target.setHeldItem(EnumHand.MAIN_HAND, null);

			target.playSound(WizardrySounds.SPELL_CONJURATION, 1.0F, 1.0f);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
