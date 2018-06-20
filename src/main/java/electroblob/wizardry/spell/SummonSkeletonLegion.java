package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class SummonSkeletonLegion extends SpellMinion<EntitySkeletonMinion> {

	public SummonSkeletonLegion(){
		super("summon_skeleton_legion", Tier.MASTER, Element.NECROMANCY, 100, 400, EntitySkeletonMinion::new, 1200, SoundEvents.ENTITY_WITHER_SPAWN);
		this.soundValues(1, 1.1f, 0.1f);
		this.quantity(3);
		this.range(3);
	}
	
	@Override
	protected void addMinionExtras(EntitySkeletonMinion minion, EntityLivingBase caster, SpellModifiers modifiers, int alreadySpawned){
		
		if(alreadySpawned % 2 == 0){
			// Archers
			minion.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		}else{
			// Swordsmen
			minion.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		}
		
		minion.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
		minion.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
		minion.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0f);
		minion.setDropChance(EntityEquipmentSlot.HEAD, 0.0f);
		minion.setDropChance(EntityEquipmentSlot.CHEST, 0.0f);
	}

}
