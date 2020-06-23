package electroblob.wizardry.client.model;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockCrystal;
import electroblob.wizardry.block.BlockPedestal;
import electroblob.wizardry.block.BlockRunestone;
import electroblob.wizardry.item.IMultiTexturedItem;
import electroblob.wizardry.item.ItemBlockMultiTextured;
import electroblob.wizardry.item.ItemCrystal;
import electroblob.wizardry.item.ItemSpectralDust;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.block.BlockPlanks;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Class responsible for registering all of wizardry's item and block models.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public final class WizardryModels {

	private WizardryModels(){} // No instances!

	@SubscribeEvent
	public static void register(ModelRegistryEvent event){

		// ItemBlocks

		registerItemModel(Item.getItemFromBlock(WizardryBlocks.arcane_workbench));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.crystal_ore));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.crystal_flower));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.transportation_stone));

		ModelLoader.setCustomStateMapper(WizardryBlocks.crystal_block, new StateMap.Builder()
				.withName(BlockCrystal.ELEMENT).withSuffix("_crystal_block").build());
		// Yay unchecked casting! But we know it's always ok here, and it makes everything much neater.
		ItemBlockMultiTextured crystalBlockItem = (ItemBlockMultiTextured)Item.getItemFromBlock(WizardryBlocks.crystal_block);
		registerMultiTexturedModel(crystalBlockItem);

		ModelLoader.setCustomStateMapper(WizardryBlocks.runestone, new StateMap.Builder()
				.withName(BlockRunestone.ELEMENT).withSuffix("_runestone").build());
		ItemBlockMultiTextured runestoneItem = (ItemBlockMultiTextured)Item.getItemFromBlock(WizardryBlocks.runestone);
		registerMultiTexturedModel(runestoneItem);

		ModelLoader.setCustomStateMapper(WizardryBlocks.runestone_pedestal, new StateMap.Builder()
				.withName(BlockPedestal.ELEMENT).ignore(BlockPedestal.NATURAL).withSuffix("_runestone_pedestal").build()); // Don't care about NATURAL property
		ItemBlockMultiTextured pedestalItem = (ItemBlockMultiTextured)Item.getItemFromBlock(WizardryBlocks.runestone_pedestal);
		registerMultiTexturedModel(pedestalItem);

		ModelLoader.setCustomStateMapper(WizardryBlocks.gilded_wood, new StateMap.Builder()
				.withName(BlockPlanks.VARIANT).withSuffix("_gilded_wood").build());
		ItemBlockMultiTextured gildedWoodItem = (ItemBlockMultiTextured)Item.getItemFromBlock(WizardryBlocks.gilded_wood);
		registerMultiTexturedModel(gildedWoodItem);

		// Explanation for all this here -> https://github.com/TheGreyGhost/MinecraftByExample/tree/master/src/main/java/minecraftbyexample/mbe05_block_dynamic_block_model2
		ModelLoaderRegistry.registerLoader(new ModelLoaderBookshelf());

		registerItemModel(Item.getItemFromBlock(WizardryBlocks.oak_bookshelf));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.spruce_bookshelf));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.birch_bookshelf));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.jungle_bookshelf));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.acacia_bookshelf));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.dark_oak_bookshelf));

		registerItemModel(Item.getItemFromBlock(WizardryBlocks.oak_lectern));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.spruce_lectern));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.birch_lectern));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.jungle_lectern));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.acacia_lectern));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.dark_oak_lectern));

		registerItemModel(Item.getItemFromBlock(WizardryBlocks.receptacle));
		registerItemModel(Item.getItemFromBlock(WizardryBlocks.imbuement_altar));

		// Items

		registerMultiTexturedModel((ItemCrystal)WizardryItems.magic_crystal);

		registerWandModel(WizardryItems.magic_wand);
		registerWandModel(WizardryItems.apprentice_wand);
		registerWandModel(WizardryItems.advanced_wand);
		registerWandModel(WizardryItems.master_wand);

		registerItemModel(WizardryItems.spell_book);
		// Wildcard registered for wizard trades.
		registerItemModel(WizardryItems.spell_book, OreDictionary.WILDCARD_VALUE, "normal");
		registerItemModel(WizardryItems.arcane_tome);
		registerItemModel(WizardryItems.wizard_handbook);

		registerWandModel(WizardryItems.novice_fire_wand);
		registerWandModel(WizardryItems.novice_ice_wand);
		registerWandModel(WizardryItems.novice_lightning_wand);
		registerWandModel(WizardryItems.novice_necromancy_wand);
		registerWandModel(WizardryItems.novice_earth_wand);
		registerWandModel(WizardryItems.novice_sorcery_wand);
		registerWandModel(WizardryItems.novice_healing_wand);

		registerWandModel(WizardryItems.apprentice_fire_wand);
		registerWandModel(WizardryItems.apprentice_ice_wand);
		registerWandModel(WizardryItems.apprentice_lightning_wand);
		registerWandModel(WizardryItems.apprentice_necromancy_wand);
		registerWandModel(WizardryItems.apprentice_earth_wand);
		registerWandModel(WizardryItems.apprentice_sorcery_wand);
		registerWandModel(WizardryItems.apprentice_healing_wand);

		registerWandModel(WizardryItems.advanced_fire_wand);
		registerWandModel(WizardryItems.advanced_ice_wand);
		registerWandModel(WizardryItems.advanced_lightning_wand);
		registerWandModel(WizardryItems.advanced_necromancy_wand);
		registerWandModel(WizardryItems.advanced_earth_wand);
		registerWandModel(WizardryItems.advanced_sorcery_wand);
		registerWandModel(WizardryItems.advanced_healing_wand);

		registerWandModel(WizardryItems.master_fire_wand);
		registerWandModel(WizardryItems.master_ice_wand);
		registerWandModel(WizardryItems.master_lightning_wand);
		registerWandModel(WizardryItems.master_necromancy_wand);
		registerWandModel(WizardryItems.master_earth_wand);
		registerWandModel(WizardryItems.master_sorcery_wand);
		registerWandModel(WizardryItems.master_healing_wand);

		registerItemModel(WizardryItems.spectral_sword);
		registerItemModel(WizardryItems.spectral_pickaxe);
		registerItemModel(WizardryItems.spectral_bow);

		registerItemModel(WizardryItems.small_mana_flask);
		registerItemModel(WizardryItems.medium_mana_flask);
		registerItemModel(WizardryItems.large_mana_flask);

		registerItemModel(WizardryItems.crystal_shard);
		registerItemModel(WizardryItems.grand_crystal);

		registerItemModel(WizardryItems.astral_diamond);

		registerItemModel(WizardryItems.purifying_elixir);

		registerItemModel(WizardryItems.storage_upgrade);
		registerItemModel(WizardryItems.siphon_upgrade);
		registerItemModel(WizardryItems.condenser_upgrade);
		registerItemModel(WizardryItems.range_upgrade);
		registerItemModel(WizardryItems.duration_upgrade);
		registerItemModel(WizardryItems.cooldown_upgrade);
		registerItemModel(WizardryItems.blast_upgrade);
		registerItemModel(WizardryItems.attunement_upgrade);
		registerItemModel(WizardryItems.melee_upgrade);

		registerItemModel(WizardryItems.flaming_axe);
		registerItemModel(WizardryItems.frost_axe);

		registerItemModel(WizardryItems.firebomb);
		registerItemModel(WizardryItems.poison_bomb);
		registerItemModel(WizardryItems.smoke_bomb);
		registerItemModel(WizardryItems.spark_bomb);

		registerItemModel(WizardryItems.blank_scroll);
		registerItemModel(WizardryItems.scroll);
		registerItemModel(WizardryItems.identification_scroll);

		registerItemModel(WizardryItems.armour_upgrade);

		registerItemModel(WizardryItems.magic_silk);

		registerMultiTexturedModel((ItemSpectralDust)WizardryItems.spectral_dust);

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

		registerItemModel(WizardryItems.lightning_hammer);

		registerItemModel(WizardryItems.ring_condensing);
		registerItemModel(WizardryItems.ring_siphoning);
		registerItemModel(WizardryItems.ring_battlemage);
		registerItemModel(WizardryItems.ring_combustion);
		registerItemModel(WizardryItems.ring_fire_melee);
		registerItemModel(WizardryItems.ring_fire_biome);
		registerItemModel(WizardryItems.ring_disintegration);
		registerItemModel(WizardryItems.ring_ice_melee);
		registerItemModel(WizardryItems.ring_ice_biome);
		registerItemModel(WizardryItems.ring_arcane_frost);
		registerItemModel(WizardryItems.ring_shattering);
		registerItemModel(WizardryItems.ring_lightning_melee);
		registerItemModel(WizardryItems.ring_storm);
		registerItemModel(WizardryItems.ring_seeking);
		registerItemModel(WizardryItems.ring_hammer);
		registerItemModel(WizardryItems.ring_soulbinding);
		registerItemModel(WizardryItems.ring_leeching);
		registerItemModel(WizardryItems.ring_necromancy_melee);
		registerItemModel(WizardryItems.ring_mind_control);
		registerItemModel(WizardryItems.ring_poison);
		registerItemModel(WizardryItems.ring_earth_melee);
		registerItemModel(WizardryItems.ring_earth_biome);
		registerItemModel(WizardryItems.ring_full_moon);
		registerItemModel(WizardryItems.ring_extraction);
		registerItemModel(WizardryItems.ring_mana_return);
		registerItemModel(WizardryItems.ring_blockwrangler);
		registerItemModel(WizardryItems.ring_conjurer);
		registerItemModel(WizardryItems.ring_defender);
		registerItemModel(WizardryItems.ring_paladin);
		registerItemModel(WizardryItems.ring_interdiction);

		registerItemModel(WizardryItems.amulet_arcane_defence);
		registerItemModel(WizardryItems.amulet_warding);
		registerItemModel(WizardryItems.amulet_wisdom);
		registerItemModel(WizardryItems.amulet_fire_protection);
		registerItemModel(WizardryItems.amulet_fire_cloaking);
		registerItemModel(WizardryItems.amulet_ice_immunity);
		registerItemModel(WizardryItems.amulet_ice_protection);
		registerItemModel(WizardryItems.amulet_potential);
		registerItemModel(WizardryItems.amulet_channeling);
		registerItemModel(WizardryItems.amulet_lich);
		registerItemModel(WizardryItems.amulet_wither_immunity);
		registerItemModel(WizardryItems.amulet_glide);
		registerItemModel(WizardryItems.amulet_banishing);
		registerItemModel(WizardryItems.amulet_anchoring);
		registerItemModel(WizardryItems.amulet_recovery);
		registerItemModel(WizardryItems.amulet_transience);
		registerItemModel(WizardryItems.amulet_resurrection);
		registerItemModel(WizardryItems.amulet_auto_shield);
		registerItemModel(WizardryItems.amulet_absorption);

		registerItemModel(WizardryItems.charm_haggler);
		registerItemModel(WizardryItems.charm_experience_tome);
		registerItemModel(WizardryItems.charm_move_speed);
		registerItemModel(WizardryItems.charm_spell_discovery);
		registerItemModel(WizardryItems.charm_auto_smelt);
		registerItemModel(WizardryItems.charm_lava_walking);
		registerItemModel(WizardryItems.charm_storm);
		registerItemModel(WizardryItems.charm_minion_health);
		registerItemModel(WizardryItems.charm_minion_variants);
		registerItemModel(WizardryItems.charm_flight);
		registerItemModel(WizardryItems.charm_growth);
		registerItemModel(WizardryItems.charm_abseiling);
		registerItemModel(WizardryItems.charm_silk_touch);
		registerItemModel(WizardryItems.charm_stop_time);
		registerItemModel(WizardryItems.charm_light);
		registerItemModel(WizardryItems.charm_transportation);
		registerItemModel(WizardryItems.charm_black_hole);
		registerItemModel(WizardryItems.charm_mount_teleporting);
		registerItemModel(WizardryItems.charm_feeding);

	}

	@SubscribeEvent
	public static void bake(ModelBakeEvent event){
		// MMMmmmm I love the smell of freshly-baked models...
		// This stuff is the boilerplate for making runestone overlay render with full brightness
		// See https://www.minecraftforge.net/forum/topic/66005-how-do-i-make-a-tileentityspecialrenderer-solved-with-ibakedmodel/
		// As usual the Forge documentation is just a description of each class and not an explanation of how to use them
		// I had to work out where this goes from the refined storage repo linked in the above thread, which is mixed in
		// with a more extensive registration system (which is super neat, but it's overkill for our purposes)
		// https://github.com/raoulvdberge/refinedstorage/blob/13d6e7f2b92f41b5009187aa2cbde50dbc72082f/src/main/java/com/raoulvdberge/refinedstorage/proxy/ProxyClient.java#L59

		for(ModelResourceLocation location : event.getModelRegistry().getKeys()){

			if(location.getNamespace().equals(Wizardry.MODID)){

				IBakedModel original = event.getModelRegistry().getObject(location);

				if(location.getPath().contains("runestone") || location.getPath().contains("runestone_pedestal")
						|| location.getPath().endsWith("imbuement_altar")){ // Ends with to exclude inactive version
					event.getModelRegistry().putObject(location, new BakedModelGlowingOverlay(original, "overlay"));
				}else if(location.getPath().contains("spectral_block")){
					event.getModelRegistry().putObject(location, new BakedModelGlowingOverlay(original, "spectral_block"));
				}
			}
		}
	}

	// Moved from the proxies

	/**
	 * Registers an item model, using the item's registry name as the model name (this convention makes it easier to
	 * keep track of everything). Variant defaults to "normal". Registers the model for all metadata values.
	 */
	private static void registerItemModel(Item item){
		// Changing the last parameter from null to "inventory" fixed the item/block model weirdness. No idea why!
		ModelBakery.registerItemVariants(item, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		// Assigns the model for all metadata values
		ModelLoader.setCustomMeshDefinition(item, s -> new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	/**
	 * Registers a wand model, using the item's registry name as the model name (this convention makes it easier to
	 * keep track of everything).
	 */
	private static void registerWandModel(Item item){

		if(Wizardry.tisTheSeason){
			// Changing the last parameter from null to "inventory" fixed the item/block model weirdness. No idea why!
			ModelBakery.registerItemVariants(item, new ModelResourceLocation("ebwizardry:festive_wand", "inventory"));
			// Assigns the model for all metadata values
			ModelLoader.setCustomMeshDefinition(item, s -> new ModelResourceLocation("ebwizardry:festive_wand", "inventory"));
		}else{
			registerItemModel(item);
		}

	}
	
	/**
	 * Registers an item model, using the itemstack-sensitive {@link IMultiTexturedItem#getModelName(ItemStack)} as the
	 * model name. This allows items to change their texture based on metadata/NBT. Variant defaults to "normal". Registers the
	 * model for metadata 0 automatically, plus all the other metadata values that the item can take, as defined in
	 * {@link Item#getSubItems(CreativeTabs, NonNullList)}. The creative tab supplied
	 * to the aforementioned method will be whichever one the item is in.
	 */
	private static <T extends Item & IMultiTexturedItem> void registerMultiTexturedModel(T item){

		if(item.getHasSubtypes()){
			NonNullList<ItemStack> items = NonNullList.create();
			item.getSubItems(item.getCreativeTab(), items);
			for(ItemStack stack : items){
				ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(),
						new ModelResourceLocation(item.getModelName(stack), "inventory"));
			}
		}
	}

	/**
	 * Registers an item model for the given metadata, using the item's registry name as the model name (this convention
	 * makes it easier to keep track of everything). This is intended for registering additional metadata values which
	 * aren't displayed in the creative menu, for example the wildcard spell book used in wizard trades.
	 */
	private static void registerItemModel(Item item, int metadata, String variant){
		ModelLoader.setCustomModelResourceLocation(item, metadata,
				new ModelResourceLocation(item.getRegistryName(), variant));
	}

}
