package electroblob.wizardry.spell;

import com.google.common.collect.ImmutableMap;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import java.util.Map;

public class ConjureArmour extends SpellConjuration {
	
	private static final Map<EntityEquipmentSlot, Item> SPECTRAL_ARMOUR_MAP = ImmutableMap.of(
			EntityEquipmentSlot.HEAD, WizardryItems.spectral_helmet,
			EntityEquipmentSlot.CHEST, WizardryItems.spectral_chestplate,
			EntityEquipmentSlot.LEGS, WizardryItems.spectral_leggings,
			EntityEquipmentSlot.FEET, WizardryItems.spectral_boots);

	public ConjureArmour(){
		super("conjure_armour", null);
	}
	
	@Override
	protected boolean conjureItem(EntityPlayer caster, SpellModifiers modifiers){
		
		ItemStack armour;
		boolean flag = false;

		// Used this rather than getArmorInventoryList because I need to access the slot itself
		for(EntityEquipmentSlot slot : WizardryUtilities.ARMOUR_SLOTS){
			
			if(caster.getItemStackFromSlot(slot).isEmpty() &&
					!WizardryUtilities.doesPlayerHaveItem(caster, SPECTRAL_ARMOUR_MAP.get(slot))){
				
				armour = new ItemStack(SPECTRAL_ARMOUR_MAP.get(slot));
				IConjuredItem.setDurationMultiplier(armour, modifiers.get(WizardryItems.duration_upgrade));
				// Sets a blank "ench" tag to trick the renderer into showing the enchantment effect on the armour model
				armour.getTagCompound().setTag("ench", new NBTTagList());
				caster.setItemStackToSlot(slot, armour);
				flag = true;
			}
		}
		
		return flag;
	}

}
