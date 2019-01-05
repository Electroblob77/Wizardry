package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Class responsible for defining, storing and registering all of wizardry's blocks.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@ObjectHolder(Wizardry.MODID)
@Mod.EventBusSubscriber
public final class WizardryBlocks {

	// Found a very nice way of registering things using arrays, which might make @ObjectHolder actually useful.
	// http://www.minecraftforge.net/forum/topic/49497-1112-is-using-registryevent-this-way-ok/

	// Anything set to use the material 'air' will not render, even with a TESR!

	// setSoundType should be public, but in this particular version it isn't... which is a bit of a pain.

	public static final Block arcane_workbench = null;
	public static final Block crystal_ore = null;
	public static final Block petrified_stone = null;
	public static final Block ice_statue = null;
	public static final Block magic_light = null;
	public static final Block crystal_flower = null;
	public static final Block snare = null;
	public static final Block transportation_stone = null;
	public static final Block spectral_block = null;
	public static final Block crystal_block = null;
	public static final Block meteor = null;
	public static final Block vanishing_cobweb = null;
	public static final Block runestone = null;

	/**
	 * Sets both the registry and unlocalised names of the given block, then registers it with the given registry. Use
	 * this instead of {@link Block#setRegistryName(String)} and {@link Block#setUnlocalizedName(String)} during
	 * construction, for convenience and consistency.
	 * 
	 * @param registry The registry to register the given block to.
	 * @param name The name of the block, without the mod ID or the .name stuff. The registry name will be
	 *        {@code ebwizardry:[name]}. The unlocalised name will be {@code tile.ebwizardry:[name].name}.
	 * @param item The block to register.
	 */
	public static void registerBlock(IForgeRegistry<Block> registry, String name, Block block){
		block.setRegistryName(Wizardry.MODID, name);
		block.setUnlocalizedName(block.getRegistryName().toString());
		registry.register(block);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Block> event){

		IForgeRegistry<Block> registry = event.getRegistry();
		
		registerBlock(registry, "arcane_workbench", 	new BlockArcaneWorkbench().setHardness(1.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "crystal_ore", 			new BlockCrystalOre(Material.ROCK).setHardness(3.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "petrified_stone", 		new BlockStatue(Material.ROCK).setHardness(1.5F).setResistance(10.0F));
		registerBlock(registry, "ice_statue", 			new BlockStatue(Material.ICE).setHardness(0.5F).setLightOpacity(3));
		registerBlock(registry, "magic_light", 			new BlockMagicLight(Material.CIRCUITS));
		registerBlock(registry, "crystal_flower", 		new BlockCrystalFlower(Material.PLANTS).setHardness(0.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "snare", 				new BlockSnare(Material.PLANTS).setHardness(0.0F));
		registerBlock(registry, "transportation_stone", new BlockTransportationStone(Material.ROCK).setHardness(0.3F).setLightLevel(0.5f).setLightOpacity(0).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "spectral_block", 		new BlockSpectral(Material.GLASS).setLightLevel(0.7f).setLightOpacity(0).setBlockUnbreakable().setResistance(6000000.0F));
		registerBlock(registry, "crystal_block", 		new BlockCrystal(Material.IRON).setHardness(5.0F).setResistance(10.0F).setCreativeTab(WizardryTabs.WIZARDRY));
		registerBlock(registry, "meteor", 				new Block(Material.ROCK).setLightLevel(1));
		registerBlock(registry, "vanishing_cobweb", 	new BlockVanishingCobweb(Material.WEB).setLightOpacity(1).setHardness(4.0F));
		registerBlock(registry, "runestone", 			new BlockRunestone(Material.ROCK));

	}
}