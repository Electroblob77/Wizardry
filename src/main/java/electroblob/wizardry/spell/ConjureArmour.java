package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

public class ConjureArmour extends SpellConjuration {

	public ConjureArmour(){
		super("conjure_armour", Tier.ADVANCED, Element.HEALING, SpellType.DEFENCE, 45, 50, null, WizardrySounds.SPELL_CONJURATION);
	}
	
	@Override
	protected boolean conjureItem(EntityPlayer caster, SpellModifiers modifiers){
		
		ItemStack armour;
		boolean flag = false;

		// Used this rather than getArmorInventoryList because I need to access the slot itself
		for(EntityEquipmentSlot slot : WizardryUtilities.ARMOUR_SLOTS){
			
			if(caster.getItemStackFromSlot(slot).isEmpty() &&
					!WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.SPECTRAL_ARMOUR_MAP.get(slot))){
				
				armour = new ItemStack(WizardryItems.SPECTRAL_ARMOUR_MAP.get(slot));
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
