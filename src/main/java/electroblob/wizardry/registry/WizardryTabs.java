package electroblob.wizardry.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.spell.Spell;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class responsible for defining and storing all of wizardry's creative tabs. Also handles sorting of the items.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
public final class WizardryTabs {

	private static Comparator<ItemStack> itemSorter;
	private static Comparator<ItemStack> spellItemSorter;

	// Creative Tabs
	public static final CreativeTabs WIZARDRY = new CreativeTabs("ebwizardry"){

		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack createIcon(){
			return new ItemStack(WizardryItems.wizard_handbook);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void displayAllRelevantItems(NonNullList<ItemStack> items){
			super.displayAllRelevantItems(items);
			Collections.sort(items, itemSorter);
		}
	};
	
	public static final CreativeTabs SPELLS = new CreativeTabs("ebwizardryspells"){

		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack createIcon(){
			return new ItemStack(WizardryItems.spell_book);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void displayAllRelevantItems(NonNullList<ItemStack> items){
			super.displayAllRelevantItems(items);
			Collections.sort(items, spellItemSorter);
		}

		@Override
		public boolean hasSearchBar(){
			return true;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public String getBackgroundImageName(){
			return "item_search.png";
		}
	};

	/** Initialises the item sorters for the creative tabs. */
	public static void sort(){

		List<Item> orderedItemList = Arrays.asList(

				Item.getItemFromBlock(WizardryBlocks.arcane_workbench),
				Item.getItemFromBlock(WizardryBlocks.crystal_ore), Item.getItemFromBlock(WizardryBlocks.crystal_block),
				Item.getItemFromBlock(WizardryBlocks.crystal_flower),
				Item.getItemFromBlock(WizardryBlocks.transportation_stone), WizardryItems.magic_crystal,
				WizardryItems.magic_wand, WizardryItems.apprentice_wand, WizardryItems.advanced_wand,
				WizardryItems.master_wand, WizardryItems.arcane_tome, WizardryItems.wizard_handbook,
				WizardryItems.basic_fire_wand, WizardryItems.basic_ice_wand, WizardryItems.basic_lightning_wand,
				WizardryItems.basic_necromancy_wand, WizardryItems.basic_earth_wand, WizardryItems.basic_sorcery_wand,
				WizardryItems.basic_healing_wand, WizardryItems.smoke_bomb, WizardryItems.firebomb,
				WizardryItems.poison_bomb, WizardryItems.blank_scroll, WizardryItems.identification_scroll,
				WizardryItems.mana_flask, WizardryItems.storage_upgrade, WizardryItems.siphon_upgrade,
				WizardryItems.condenser_upgrade, WizardryItems.range_upgrade, WizardryItems.duration_upgrade,
				WizardryItems.cooldown_upgrade, WizardryItems.blast_upgrade, WizardryItems.attunement_upgrade,
				WizardryItems.magic_silk, WizardryItems.armour_upgrade, WizardryItems.wizard_hat,
				WizardryItems.wizard_robe, WizardryItems.wizard_leggings, WizardryItems.wizard_boots,
				WizardryItems.wizard_hat_fire, WizardryItems.wizard_robe_fire, WizardryItems.wizard_leggings_fire,
				WizardryItems.wizard_boots_fire, WizardryItems.wizard_hat_ice, WizardryItems.wizard_robe_ice,
				WizardryItems.wizard_leggings_ice, WizardryItems.wizard_boots_ice, WizardryItems.wizard_hat_lightning,
				WizardryItems.wizard_robe_lightning, WizardryItems.wizard_leggings_lightning,
				WizardryItems.wizard_boots_lightning, WizardryItems.wizard_hat_necromancy,
				WizardryItems.wizard_robe_necromancy, WizardryItems.wizard_leggings_necromancy,
				WizardryItems.wizard_boots_necromancy, WizardryItems.wizard_hat_earth, WizardryItems.wizard_robe_earth,
				WizardryItems.wizard_leggings_earth, WizardryItems.wizard_boots_earth, WizardryItems.wizard_hat_sorcery,
				WizardryItems.wizard_robe_sorcery, WizardryItems.wizard_leggings_sorcery,
				WizardryItems.wizard_boots_sorcery, WizardryItems.wizard_hat_healing, WizardryItems.wizard_robe_healing,
				WizardryItems.wizard_leggings_healing, WizardryItems.wizard_boots_healing);

		itemSorter = (stack1, stack2) -> {
			// Neither stack is in the creative tab
			if(!orderedItemList.contains(stack1.getItem()) && !orderedItemList.contains(stack2.getItem())) return 0;
			if(!orderedItemList.contains(stack1.getItem())) return 1; // Only stack 2 is in the creative tab
			if(!orderedItemList.contains(stack2.getItem())) return -1; // Only stack 1 is in the creative tab
			// Both stacks are in the creative tab
			return orderedItemList.indexOf(stack1.getItem()) - orderedItemList.indexOf(stack2.getItem());
		};

		spellItemSorter = (stack1, stack2) -> {

            if((stack1.getItem() instanceof ItemSpellBook && stack2.getItem() instanceof ItemSpellBook)
                    || (stack1.getItem() instanceof ItemScroll && stack2.getItem() instanceof ItemScroll)){

                Spell spell1 = Spell.get(stack1.getItemDamage());
                Spell spell2 = Spell.get(stack2.getItemDamage());

                return spell1.compareTo(spell2);

            }else if(stack1.getItem() instanceof ItemScroll){
                return 1;
            }else if(stack2.getItem() instanceof ItemScroll){
                return -1;
            }
            return 0;
        };
	}

}
