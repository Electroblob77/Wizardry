package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonSkeletonLegion extends Spell {

	public SummonSkeletonLegion(){
		super(Tier.MASTER, 100, Element.NECROMANCY, "summon_skeleton_legion", SpellType.MINION, 400, EnumAction.BOW,
				false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			// Archers
			for(int i = 0; i < 3; i++){
				// This is all so much neater now thanks to this method.
				BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 3, 6);
				// The spell instantly fails if no space was found (see javadoc for the above method).
				if(pos == null) return false;

				EntitySkeletonMinion skeleton = new EntitySkeletonMinion(world, pos.getX() + 0.5, pos.getY(),
						pos.getZ() + 0.5, caster, (int)(1200 * modifiers.get(WizardryItems.duration_upgrade)));
				skeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
				skeleton.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
				skeleton.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
				skeleton.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0f);
				skeleton.setDropChance(EntityEquipmentSlot.HEAD, 0.0f);
				skeleton.setDropChance(EntityEquipmentSlot.CHEST, 0.0f);
				world.spawnEntity(skeleton);
			}
			// Swordsmen
			for(int i = 0; i < 3; i++){

				BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 3, 6);
				// The spell instantly fails if no space was found (see javadoc for the above method).
				if(pos == null) return false;

				EntitySkeletonMinion skeleton = new EntitySkeletonMinion(world, pos.getX() + 0.5, pos.getY(),
						pos.getZ() + 0.5, caster, (int)(1200 * modifiers.get(WizardryItems.duration_upgrade)));
				skeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
				skeleton.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
				skeleton.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
				skeleton.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0f);
				skeleton.setDropChance(EntityEquipmentSlot.HEAD, 0.0f);
				skeleton.setDropChance(EntityEquipmentSlot.CHEST, 0.0f);
				world.spawnEntity(skeleton);
			}
		}

		// Can't possibly get this far if nothing was spawned.
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SPAWN, 1.0F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

}
