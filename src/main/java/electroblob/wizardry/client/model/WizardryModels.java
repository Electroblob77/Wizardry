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

import java.util.ArrayList;
import java.util.List;

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

	/** Keeps track of all items whose models have been registered manually to exclude them from automatic registry of
	 * standard item models. Internal only, this gets cleared once model registry is complete. */
	private static final List<Item> registeredItems = new ArrayList<>();

	@SubscribeEvent
	public static void register(ModelRegistryEvent event){

		// ItemBlocks
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

		// Items

		//registerMultiTexturedModel((ItemCrystal)WizardryItems.magic_crystal);

		registerWandModel(WizardryItems.magic_wand);
		registerWandModel(WizardryItems.apprentice_wand);
		registerWandModel(WizardryItems.advanced_wand);
		registerWandModel(WizardryItems.master_wand);

		registerItemModel(WizardryItems.spell_book); // Also need this or auto-registry will ignore it
		// Wildcard registered for wizard trades.
		registerItemModel(WizardryItems.spell_book, OreDictionary.WILDCARD_VALUE, "normal");

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

		// Automatic item model registry
		for(Item item : Item.REGISTRY){
			if(!registeredItems.contains(item) && item.getRegistryName().getNamespace().equals(Wizardry.MODID)){
				registerItemModel(item); // Standard item model
			}
		}

		registeredItems.clear(); // Might as well clean this up
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
		registeredItems.add(item);
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
			registeredItems.add(item);
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

		registeredItems.add(item);
	}

	/**
	 * Registers an item model for the given metadata, using the item's registry name as the model name (this convention
	 * makes it easier to keep track of everything). This is intended for registering additional metadata values which
	 * aren't displayed in the creative menu, for example the wildcard spell book used in wizard trades.
	 */
	private static void registerItemModel(Item item, int metadata, String variant){
		ModelLoader.setCustomModelResourceLocation(item, metadata,
				new ModelResourceLocation(item.getRegistryName(), variant));
		registeredItems.add(item); // Still ought to do this in case I ever use this method alone for an item
	}

}
