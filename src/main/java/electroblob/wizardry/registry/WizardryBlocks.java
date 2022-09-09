package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.*;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

/**
 * Class responsible for defining, storing and registering all of wizardry's blocks. Also handles registry of the
 * tile entities.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@ObjectHolder(Wizardry.MODID)
@Mod.EventBusSubscriber
public final class WizardryBlocks {

	private WizardryBlocks(){} // No instances!

	// Found a very nice way of registering things using arrays, which might make @ObjectHolder actually useful.
	// http://www.minecraftforge.net/forum/topic/49497-1112-is-using-registryevent-this-way-ok/

	// Anything set to use the material 'air' will not render, even with a TESR!

	// setSoundType should be public, but in this particular version it isn't... which is a bit of a pain.

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder(){ return null; }

	public static final Block arcane_workbench = placeholder();
	public static final Block crystal_ore = placeholder();
	public static final Block crystal_flower = placeholder();
	public static final Block transportation_stone = placeholder();

	public static final Block magic_crystal_block = placeholder();
	public static final Block fire_crystal_block = placeholder();
	public static final Block ice_crystal_block = placeholder();
	public static final Block lightning_crystal_block = placeholder();
	public static final Block necromancy_crystal_block = placeholder();
	public static final Block earth_crystal_block = placeholder();
	public static final Block sorcery_crystal_block = placeholder();
	public static final Block healing_crystal_block = placeholder();

	public static final Block petrified_stone = placeholder();
	public static final Block ice_statue = placeholder();
	public static final Block magic_light = placeholder();
	public static final Block snare = placeholder();
	public static final Block spectral_block = placeholder();
	public static final Block meteor = placeholder();
	public static final Block vanishing_cobweb = placeholder();
	public static final Block thorns = placeholder();
	public static final Block obsidian_crust = placeholder();
	public static final Block dry_frosted_ice = placeholder();
	public static final Block crystal_flower_pot = placeholder();
	public static final Block permafrost = placeholder();

	public static final Block runestone = placeholder();
	public static final Block runestone_pedestal = placeholder();

	public static final Block gilded_wood = placeholder();

	public static final Block oak_bookshelf = placeholder();
	public static final Block spruce_bookshelf = placeholder();
	public static final Block birch_bookshelf = placeholder();
	public static final Block jungle_bookshelf = placeholder();
	public static final Block acacia_bookshelf = placeholder();
	public static final Block dark_oak_bookshelf = placeholder();

	public static final Block oak_lectern = placeholder();
	public static final Block spruce_lectern = placeholder();
	public static final Block birch_lectern = placeholder();
	public static final Block jungle_lectern = placeholder();
	public static final Block acacia_lectern = placeholder();
	public static final Block dark_oak_lectern = placeholder();

	public static final Block receptacle = placeholder();
	public static final Block imbuement_altar = placeholder();

	/**
	 * Sets both the registry and unlocalised names of the given block, then registers it with the given registry. Use
	 * this instead of {@link Block#setRegistryName(String)} and {@link Block#setTranslationKey(String)} during
	 * construction, for convenience and consistency.
	 * 
	 * @param registry The registry to register the given block to.
	 * @param name The name of the block, without the mod ID or the .name stuff. The registry name will be
	 *        {@code ebwizardry:[name]}. The unlocalised name will be {@code tile.ebwizardry:[name].name}.
	 * @param block The block to register.
	 */
	public static void registerBlock(IForgeRegistry<Block> registry, String name, Block block){
		block.setRegistryName(Wizardry.MODID, name);
		block.setTranslationKey(block.getRegistryName().toString());
		registry.register(block);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Block> event){

		BlockBookshelf.initBookProperties();

		IForgeRegistry<Block> registry = event.getRegistry();

		// TODO: Put everything in block classes wherever possible
		registerBlock(registry, "arcane_workbench", 		new BlockArcaneWorkbench().setHardness(1.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "crystal_ore", 			new BlockCrystalOre(Material.ROCK).setHardness(3.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "crystal_flower", 		new BlockCrystalFlower(Material.PLANTS).setHardness(0.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "transportation_stone", 	new BlockTransportationStone(Material.ROCK).setHardness(0.3F).setLightLevel(0.5f).setLightOpacity(0).setCreativeTab(WizardryTabs.WIZARDRY));

		registerBlock(registry, "magic_crystal_block", 			new BlockCrystal(Element.MAGIC).setHardness(5.0F).setResistance(10.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "fire_crystal_block", 			new BlockCrystal(Element.MAGIC).setHardness(5.0F).setResistance(10.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "ice_crystal_block", 				new BlockCrystal(Element.MAGIC).setHardness(5.0F).setResistance(10.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "lightning_crystal_block", 		new BlockCrystal(Element.MAGIC).setHardness(5.0F).setResistance(10.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "necromancy_crystal_block", 		new BlockCrystal(Element.MAGIC).setHardness(5.0F).setResistance(10.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "earth_crystal_block", 			new BlockCrystal(Element.MAGIC).setHardness(5.0F).setResistance(10.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "sorcery_crystal_block", 			new BlockCrystal(Element.MAGIC).setHardness(5.0F).setResistance(10.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "healing_crystal_block", 			new BlockCrystal(Element.MAGIC).setHardness(5.0F).setResistance(10.0F).setCreativeTab(WizardryTabs.WIZARDRY));

		registerBlock(registry, "petrified_stone", 		new BlockStatue(Material.ROCK).setHardness(1.5F).setResistance(10.0F));
		registerBlock(registry, "ice_statue", 			new BlockStatue(Material.ICE).setHardness(0.5F).setLightOpacity(3));
		registerBlock(registry, "magic_light", 			new BlockMagicLight(Material.CIRCUITS));
		registerBlock(registry, "snare", 					new BlockSnare(Material.PLANTS).setHardness(0.0F));
		registerBlock(registry, "spectral_block", 		new BlockSpectral(Material.GLASS).setLightOpacity(1).setBlockUnbreakable().setResistance(6000000.0F));
		registerBlock(registry, "meteor", 				new Block(Material.ROCK).setLightLevel(1));
		registerBlock(registry, "vanishing_cobweb", 		new BlockVanishingCobweb(Material.WEB).setLightOpacity(1).setHardness(4.0F));
		registerBlock(registry, "thorns", 				new BlockThorns());
		registerBlock(registry, "obsidian_crust", 		new BlockObsidianCrust());
		registerBlock(registry, "dry_frosted_ice", 		new BlockDryFrostedIce());
		registerBlock(registry, "crystal_flower_pot", 	new BlockCrystalFlowerPot());
		registerBlock(registry, "permafrost", 			new BlockPermafrost());

		registerBlock(registry, "runestone", 				new BlockRunestone(Material.ROCK));
		registerBlock(registry, "runestone_pedestal", 	new BlockPedestal(Material.ROCK));

		registerBlock(registry, "gilded_wood", 			new BlockGildedWood());

		registerBlock(registry, "oak_bookshelf", 			new BlockBookshelf());
		registerBlock(registry, "spruce_bookshelf", 		new BlockBookshelf());
		registerBlock(registry, "birch_bookshelf", 		new BlockBookshelf());
		registerBlock(registry, "jungle_bookshelf", 		new BlockBookshelf());
		registerBlock(registry, "acacia_bookshelf", 		new BlockBookshelf());
		registerBlock(registry, "dark_oak_bookshelf", 	new BlockBookshelf());

		registerBlock(registry, "oak_lectern", 			new BlockLectern());
		registerBlock(registry, "birch_lectern", 			new BlockLectern());
		registerBlock(registry, "spruce_lectern", 		new BlockLectern());
		registerBlock(registry, "jungle_lectern", 		new BlockLectern());
		registerBlock(registry, "acacia_lectern", 		new BlockLectern());
		registerBlock(registry, "dark_oak_lectern", 		new BlockLectern());

		registerBlock(registry, "receptacle", 			new BlockReceptacle());
		registerBlock(registry, "imbuement_altar", 		new BlockImbuementAltar());

	}

	/** Called from the preInit method in the main mod class to register all the tile entities. */
	public static void registerTileEntities(){
		// Nope, these still don't have their own registry...
		GameRegistry.registerTileEntity(TileEntityArcaneWorkbench.class, 	new ResourceLocation(Wizardry.MODID, "arcane_workbench"));
		GameRegistry.registerTileEntity(TileEntityStatue.class, 			new ResourceLocation(Wizardry.MODID, "petrified_stone"));
		GameRegistry.registerTileEntity(TileEntityMagicLight.class, 		new ResourceLocation(Wizardry.MODID, "magic_light"));
		GameRegistry.registerTileEntity(TileEntityTimer.class, 				new ResourceLocation(Wizardry.MODID, "timer"));
		GameRegistry.registerTileEntity(TileEntityPlayerSave.class, 		new ResourceLocation(Wizardry.MODID, "player_save"));
		GameRegistry.registerTileEntity(TileEntityThorns.class, 			new ResourceLocation(Wizardry.MODID, "player_save_timed"));
		GameRegistry.registerTileEntity(TileEntityShrineCore.class, 		new ResourceLocation(Wizardry.MODID, "shrine_core"));
		GameRegistry.registerTileEntity(TileEntityBookshelf.class, 			new ResourceLocation(Wizardry.MODID, "bookshelf"));
		GameRegistry.registerTileEntity(TileEntityLectern.class, 			new ResourceLocation(Wizardry.MODID, "lectern"));
		GameRegistry.registerTileEntity(TileEntityReceptacle.class, 		new ResourceLocation(Wizardry.MODID, "receptacle"));
		GameRegistry.registerTileEntity(TileEntityImbuementAltar.class, 	new ResourceLocation(Wizardry.MODID, "imbuement_altar"));
	}
}