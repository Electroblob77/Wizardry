package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonSkeleton extends Spell {

	public SummonSkeleton(){
		super(Tier.APPRENTICE, 15, Element.NECROMANCY, "summon_skeleton", SpellType.MINION, 50, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){

			BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;

			EntitySkeletonMinion skeleton = new EntitySkeletonMinion(world, pos.getX() + 0.5, pos.getY(),
					pos.getZ() + 0.5, caster, (int)(600 * modifiers.get(WizardryItems.duration_upgrade)));
			skeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
			skeleton.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0f);
			world.spawnEntity(skeleton);
		}
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SUMMONING, 7.0f, 0.6f);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(!world.isRemote){

			BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;

			EntitySkeletonMinion skeleton = new EntitySkeletonMinion(world, pos.getX() + 0.5, pos.getY(),
					pos.getZ() + 0.5, caster, (int)(600 * modifiers.get(WizardryItems.duration_upgrade)));
			skeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
			skeleton.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0f);
			world.spawnEntity(skeleton);
		}
		caster.playSound(WizardrySounds.SPELL_SUMMONING, 7.0f, 0.6f);
		return true;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
