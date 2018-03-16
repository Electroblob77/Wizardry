package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockArcaneWorkbench;
import electroblob.wizardry.block.BlockCrystalFlower;
import electroblob.wizardry.block.BlockCrystalOre;
import electroblob.wizardry.block.BlockMagicLight;
import electroblob.wizardry.block.BlockSnare;
import electroblob.wizardry.block.BlockSpectral;
import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.block.BlockTransportationStone;
import electroblob.wizardry.block.BlockVanishingCobweb;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Class responsible for defining, storing and registering all of wizardry's blocks.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardryBlocks {

	// I get registry events, they make sense - even I doubted myself with when to do various things in the load
	// process,
	// so I can see why the folks at Forge wanted to save us having to think about it.
	// What I do not understand is why you would use @ObjectHolder for your own blocks and items. What's the point in
	// making your code longer and more complicated, when you can just define the blocks as constants and register them
	// later?

	// Found a very nice way of registering things using arrays, which might make @ObjectHolder actually useful.
	// http://www.minecraftforge.net/forum/topic/49497-1112-is-using-registryevent-this-way-ok/

	// Anything set to use the material 'air' will not render, even with a TESR!

	// setSoundType should be public, but in this particular version it isn't... which is a bit of a pain.

	public static final Block arcane_workbench = new BlockArcaneWorkbench().setHardness(1.0F)
			.setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Block crystal_ore = new BlockCrystalOre(Material.ROCK).setHardness(3.0F)
			.setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Block petrified_stone = new BlockStatue(Material.ROCK).setHardness(1.5F).setResistance(10.0F);
	public static final Block ice_statue = new BlockStatue(Material.ICE).setHardness(0.5F).setLightOpacity(3);
	public static final Block magic_light = new BlockMagicLight(Material.CIRCUITS);
	public static final Block crystal_flower = new BlockCrystalFlower(Material.PLANTS).setHardness(0.0F)
			.setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Block snare = new BlockSnare(Material.PLANTS).setHardness(0.0F);
	public static final Block transportation_stone = new BlockTransportationStone(Material.ROCK).setHardness(0.0F)
			.setLightLevel(0.5f).setLightOpacity(0).setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Block spectral_block = new BlockSpectral(Material.GLASS).setLightLevel(0.7f).setLightOpacity(0)
			.setBlockUnbreakable().setResistance(6000000.0F);
	public static final Block crystal_block = new Block(Material.IRON).setHardness(5.0F).setResistance(10.0F)
			.setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Block meteor = new Block(Material.ROCK).setLightLevel(1);
	public static final Block vanishing_cobweb = new BlockVanishingCobweb(Material.WEB).setLightOpacity(1)
			.setHardness(4.0F);

	static{
		// This is here because Block#setHarvestLevel isn't chainable.
		crystal_block.setHarvestLevel("pickaxe", 2);
	}

	/**
	 * Sets both the registry and unlocalised names of the given block, then registers it with the given registry. Use
	 * this instead of {@link Block#setRegistryName(String)} and {@link Block#setUnlocalizedName(String)} during
	 * construction, for convenience and consistency.
	 * 
	 * @param registry The registry to register the given block to.
	 * @param item The block to register.
	 * @param name The name of the block, without the mod ID or the .name stuff. The registry name will be
	 *        {@code wizardry:[name]}. The unlocalised name will be {@code tile.wizardry:[name].name}.
	 */
	public static void registerBlock(IForgeRegistry<Block> registry, Block block, String name){
		block.setRegistryName(Wizardry.MODID, name);
		block.setUnlocalizedName(block.getRegistryName().toString());
		registry.register(block);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Block> event){

		IForgeRegistry<Block> registry = event.getRegistry();

		registerBlock(registry, arcane_workbench, "arcane_workbench");
		registerBlock(registry, crystal_ore, "crystal_ore");
		registerBlock(registry, petrified_stone, "petrified_stone");
		registerBlock(registry, ice_statue, "ice_statue");
		registerBlock(registry, magic_light, "magic_light");
		registerBlock(registry, crystal_flower, "crystal_flower");
		registerBlock(registry, snare, "snare");
		registerBlock(registry, transportation_stone, "transportation_stone");
		registerBlock(registry, spectral_block, "spectral_block");
		registerBlock(registry, crystal_block, "crystal_block");
		registerBlock(registry, meteor, "meteor");
		registerBlock(registry, vanishing_cobweb, "vanishing_cobweb");
	}
}