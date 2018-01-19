package electroblob.wizardry.client.model;

import java.util.ArrayList;
import java.util.List;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Class responsible for registering all of wizardry's item (and itemblock) models.
 * @author Electroblob
 * @since Wizardry 2.1
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public final class WizardryItemModels {

	@SubscribeEvent
	public static void register(ModelRegistryEvent event){
		
		// ItemBlocks
		
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.arcane_workbench));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.crystal_ore));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.crystal_flower));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.transportation_stone));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.crystal_block));
		
		// Items
		
		registerItemModel(WizardryItems.magic_crystal);

        registerItemModel(WizardryItems.magic_wand);
        registerItemModel(WizardryItems.apprentice_wand);
        registerItemModel(WizardryItems.advanced_wand);
        registerItemModel(WizardryItems.master_wand);

        registerItemModel(WizardryItems.spell_book);
        // Wildcard registered for wizard trades.
        registerItemModel(WizardryItems.spell_book, OreDictionary.WILDCARD_VALUE, "normal");
        registerItemModel(WizardryItems.arcane_tome);
        registerItemModel(WizardryItems.wizard_handbook);
        
        registerItemModel(WizardryItems.basic_fire_wand);
        registerItemModel(WizardryItems.basic_ice_wand);
        registerItemModel(WizardryItems.basic_lightning_wand);
        registerItemModel(WizardryItems.basic_necromancy_wand);
        registerItemModel(WizardryItems.basic_earth_wand);
        registerItemModel(WizardryItems.basic_sorcery_wand);
        registerItemModel(WizardryItems.basic_healing_wand);

        registerItemModel(WizardryItems.apprentice_fire_wand);
        registerItemModel(WizardryItems.apprentice_ice_wand);
        registerItemModel(WizardryItems.apprentice_lightning_wand);
        registerItemModel(WizardryItems.apprentice_necromancy_wand);
        registerItemModel(WizardryItems.apprentice_earth_wand);
        registerItemModel(WizardryItems.apprentice_sorcery_wand);
        registerItemModel(WizardryItems.apprentice_healing_wand);

        registerItemModel(WizardryItems.advanced_fire_wand);
        registerItemModel(WizardryItems.advanced_ice_wand);
        registerItemModel(WizardryItems.advanced_lightning_wand);
        registerItemModel(WizardryItems.advanced_necromancy_wand);
        registerItemModel(WizardryItems.advanced_earth_wand);
        registerItemModel(WizardryItems.advanced_sorcery_wand);
        registerItemModel(WizardryItems.advanced_healing_wand);

        registerItemModel(WizardryItems.master_fire_wand);
        registerItemModel(WizardryItems.master_ice_wand);
        registerItemModel(WizardryItems.master_lightning_wand);
        registerItemModel(WizardryItems.master_necromancy_wand);
        registerItemModel(WizardryItems.master_earth_wand);
        registerItemModel(WizardryItems.master_sorcery_wand);
        registerItemModel(WizardryItems.master_healing_wand);

        registerItemModel(WizardryItems.spectral_sword);
        registerItemModel(WizardryItems.spectral_pickaxe);
        registerItemModel(WizardryItems.spectral_bow);

        registerItemModel(WizardryItems.mana_flask);

        registerItemModel(WizardryItems.storage_upgrade);
        registerItemModel(WizardryItems.siphon_upgrade);
        registerItemModel(WizardryItems.condenser_upgrade);
        registerItemModel(WizardryItems.range_upgrade);
        registerItemModel(WizardryItems.duration_upgrade);
        registerItemModel(WizardryItems.cooldown_upgrade);
        registerItemModel(WizardryItems.blast_upgrade);
        registerItemModel(WizardryItems.attunement_upgrade);

        registerItemModel(WizardryItems.flaming_axe);
        registerItemModel(WizardryItems.frost_axe);

        registerItemModel(WizardryItems.firebomb);
        registerItemModel(WizardryItems.poison_bomb);
        
        registerItemModel(WizardryItems.blank_scroll);
        registerItemModel(WizardryItems.scroll);

        registerItemModel(WizardryItems.armour_upgrade);
        
        registerItemModel(WizardryItems.magic_silk);
        
        registerItemModel(WizardryItems.wizard_hat);
        registerItemModel(WizardryItems.wizard_robe);
        registerItemModel(WizardryItems.wizard_leggings);
        registerItemModel(WizardryItems.wizard_boots);

        registerItemModel(WizardryItems.wizard_hat_fire);
        registerItemModel(WizardryItems.wizard_robe_fire);
        registerItemModel(WizardryItems.wizard_leggings_fire);
        registerItemModel(WizardryItems.wizard_boots_fire);

        registerItemModel(WizardryItems.wizard_hat_ice);
        registerItemModel(WizardryItems.wizard_robe_ice);
        registerItemModel(WizardryItems.wizard_leggings_ice);
        registerItemModel(WizardryItems.wizard_boots_ice);

        registerItemModel(WizardryItems.wizard_hat_lightning);
        registerItemModel(WizardryItems.wizard_robe_lightning);
        registerItemModel(WizardryItems.wizard_leggings_lightning);
        registerItemModel(WizardryItems.wizard_boots_lightning);

        registerItemModel(WizardryItems.wizard_hat_necromancy);
        registerItemModel(WizardryItems.wizard_robe_necromancy);
        registerItemModel(WizardryItems.wizard_leggings_necromancy);
        registerItemModel(WizardryItems.wizard_boots_necromancy);

        registerItemModel(WizardryItems.wizard_hat_earth);
        registerItemModel(WizardryItems.wizard_robe_earth);
        registerItemModel(WizardryItems.wizard_leggings_earth);
        registerItemModel(WizardryItems.wizard_boots_earth);
        
        registerItemModel(WizardryItems.wizard_hat_sorcery);
        registerItemModel(WizardryItems.wizard_robe_sorcery);
        registerItemModel(WizardryItems.wizard_leggings_sorcery);
        registerItemModel(WizardryItems.wizard_boots_sorcery);

        registerItemModel(WizardryItems.wizard_hat_healing);
        registerItemModel(WizardryItems.wizard_robe_healing);
        registerItemModel(WizardryItems.wizard_leggings_healing);
        registerItemModel(WizardryItems.wizard_boots_healing);

        registerItemModel(WizardryItems.spectral_helmet);
        registerItemModel(WizardryItems.spectral_chestplate);
        registerItemModel(WizardryItems.spectral_leggings);
        registerItemModel(WizardryItems.spectral_boots);
        
        registerItemModel(WizardryItems.smoke_bomb);
        
        registerItemModel(WizardryItems.identification_scroll);
	}
	
	// Moved from the proxies
	
	/** Registers an item model, using the item's registry name as the model name (this
	 * convention makes it easier to keep track of everything). Variant defaults to "normal". Registers the model
	 * for metadata 0 automatically, plus all the other metadata values that the item can take, as defined in
	 * {@link Item#getSubItems(Item, net.minecraft.creativetab.CreativeTabs, java.util.List)}. The passed in item
	 * <b>must</b> allow null to be passed in for the creative tab parameter in the aforementioned method, or a
	 * {@link NullPointerException} will result. */
	private static void registerItemModel(Item item){
		
		if(item.getHasSubtypes()){
			List<ItemStack> items = new ArrayList<ItemStack>();
			item.getSubItems(item, null, items); // Client-only method, but we're client-side so this is OK.
			for(ItemStack stack : items){
		        ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(), new ModelResourceLocation(item.getRegistryName(), "inventory"));
			}
		}
		// Changing the last parameter from null to "inventory" fixed the item/block model weirdness. No idea why!
	    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
	
	/** Registers an item model for the given metadata, using the item's registry name as the model name (this
	 * convention makes it easier to keep track of everything). This is intended for registering additional metadata
	 * values which aren't displayed in the creative menu, for example the wildcard spell book used in wizard trades. */
	private static void registerItemModel(Item item, int metadata, String variant) {
        ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(item.getRegistryName(), variant));
	}

}
