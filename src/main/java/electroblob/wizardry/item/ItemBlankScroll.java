package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemBlankScroll extends Item implements IWorkbenchItem {

	public ItemBlankScroll(){
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.addPropertyOverride(new ResourceLocation("festive"), (s, w, e) -> Wizardry.tisTheSeason ? 1 : 0);
	}

	@Override
	public int getSpellSlotCount(ItemStack stack){
		return 1;
	}

	@Override
	public boolean showTooltip(ItemStack stack){
		return false;
	}

	@Override
	public boolean onApplyButtonPressed(EntityPlayer player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks){
		
		if(!spellBooks[0].getStack().isEmpty() && !crystals.getStack().isEmpty()){
			
			Spell spell = Spell.byMetadata(spellBooks[0].getStack().getItemDamage());
			WizardData data = WizardData.get(player);

			// Spells can only be bound to scrolls if the player has already cast them (prevents casting of master
			// spells without getting a master wand)
			// This restriction does not apply in creative mode
			if(spell != Spells.none && player.isCreative() || (data != null
					&& data.hasSpellBeenDiscovered(spell)) && spell.isEnabled(SpellProperties.Context.SCROLL)){
				
				int cost = spell.getCost() * centre.getStack().getCount();
				// Continuous spell scrolls require enough mana to cast them for the duration defined in ItemScroll.
				if(spell.isContinuous) cost *= ItemScroll.CASTING_TIME / 20;

				int manaPerItem = Constants.MANA_PER_CRYSTAL;
				if(crystals.getStack().getItem() == WizardryItems.crystal_shard) manaPerItem = Constants.MANA_PER_SHARD;
				if(crystals.getStack().getItem() == WizardryItems.grand_crystal) manaPerItem = Constants.GRAND_CRYSTAL_MANA;
				
				if(crystals.getStack().getCount() * manaPerItem > cost){
					// Rounds up to the nearest whole crystal
					crystals.decrStackSize(cost / manaPerItem + 1);
					centre.putStack(new ItemStack(WizardryItems.scroll, centre.getStack().getCount(), spell.metadata()));
					return true;
				}
				
			}
		}
		
		return false;
	}

}
