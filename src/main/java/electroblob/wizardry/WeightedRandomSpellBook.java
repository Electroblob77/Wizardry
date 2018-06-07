package electroblob.wizardry;

import java.util.Random;

import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.spell.Spell;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

public class WeightedRandomSpellBook extends WeightedRandomChestContent{

	public WeightedRandomSpellBook(ItemStack par1ItemStack, int par2, int par3,
			int par4) {
		super(par1ItemStack, par2, par3, par4);
	}
	
	/* A brief comment about dungeon loot:
	 * Tiers are weighted as per usual. Elements all have equal chance, INCLUDING simple stuff. The main reason for
	 * this is that dungeon loot is not that common anyway, so making elemental gear even rarer would be too much.
	 */
	
	protected ItemStack[] generateChestContent(Random random, IInventory newInventory){
    
		if(theItemId.getItem() instanceof ItemSpellBook){
			
			theItemId = new ItemStack(theItemId.getItem(), 1, WizardryUtilities.getStandardWeightedRandomSpellId(random));
			
		}else if(theItemId.getItem() instanceof ItemWand){
			
			theItemId = new ItemStack(WizardryUtilities.getWand(EnumTier.BASIC, EnumElement.values()[random.nextInt(EnumElement.values().length)]));
			
		}else if(theItemId.getItem() instanceof ItemWizardArmour){
			
			theItemId = new ItemStack(WizardryUtilities.getArmour(EnumElement.values()[random.nextInt(EnumElement.values().length)], random.nextInt(3)));
		
		}else if(theItemId.getItem() instanceof ItemScroll){
			
			int spellId = WizardryUtilities.getStandardWeightedRandomSpellId(random, true);
			theItemId = new ItemStack(theItemId.getItem(), 1, spellId);
		
		}else if(theItemId.getItem() == Wizardry.condenserUpgrade){
			
			switch(random.nextInt(8)){
			case 0: theItemId = new ItemStack(Wizardry.condenserUpgrade); break;
			case 1: theItemId = new ItemStack(Wizardry.siphonUpgrade); break;
			case 2: theItemId = new ItemStack(Wizardry.rangeUpgrade); break;
			case 3: theItemId = new ItemStack(Wizardry.cooldownUpgrade); break;
			case 4: theItemId = new ItemStack(Wizardry.durationUpgrade); break;
			case 5: theItemId = new ItemStack(Wizardry.storageUpgrade); break;
			case 6: theItemId = new ItemStack(Wizardry.blastUpgrade); break;
			case 7: theItemId = new ItemStack(Wizardry.attunementUpgrade); break;
			}
		
		}
		
        return ChestGenHooks.generateStacks(random, theItemId, theMinimumChanceToGenerateItem, theMaximumChanceToGenerateItem);
    }
	
}
