package electroblob.wizardry.worldgen;

import com.google.common.collect.ImmutableSet;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.integration.antiqueatlas.WizardryAntiqueAtlasIntegration;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import electroblob.wizardry.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlab.EnumType;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Random;
import java.util.Set;

public class WorldGenUndergroundLibraryRuins extends WorldGenUndergroundStructure {

	private final Set<Block> nonReplaceableBlocks;

	public WorldGenUndergroundLibraryRuins(){
		nonReplaceableBlocks = ImmutableSet.of(WizardryBlocks.imbuement_altar, WizardryBlocks.receptacle,
				WizardryBlocks.oak_bookshelf);
	}

	@Override
	public String getStructureName(){
		return "underground_library_ruins";
	}

	@Override
	public long getRandomSeedModifier(){
		return 13012834L;
	}

	@Override
	public ResourceLocation getStructureFile(Random random){
		return Wizardry.settings.undergroundLibraryFiles[random.nextInt(Wizardry.settings.undergroundLibraryFiles.length)];
	}

	@Override
	public boolean canGenerate(Random random, World world, int chunkX, int chunkZ){
		return ArrayUtils.contains(Wizardry.settings.libraryDimensions, world.provider.getDimension())
				&& Wizardry.settings.libraryRarity > 0 && random.nextInt(Wizardry.settings.libraryRarity) == 0;
	}

	@Override
	public void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile){

		final Biome biome = world.getBiome(origin);
		final float stoneBrickChance = random.nextFloat();
		final float mossiness = 0.6f; // Underground is a mossier place!
		final BlockPlanks.EnumType woodType = BlockUtils.getBiomeWoodVariant(biome);

		ITemplateProcessor processor = new MultiTemplateProcessor(true,
				// Erase walls and ceilings where caves were
				(w, p, i) -> w.isAirBlock(p) && !nonReplaceableBlocks.contains(i.blockState.getBlock()) ? null : i,
				// Cobblestone/stone brick
				(w, p, i) -> {
					if(w.rand.nextFloat() > stoneBrickChance){
						// Behold, three different ways of doing the same thing, because this is pre-flattening!
						// Also, stone bricks are about the least consistently-named thing in the entire game, so yay
						if(i.blockState.getBlock() == Blocks.COBBLESTONE){
							return new Template.BlockInfo(i.pos, Blocks.STONEBRICK.getDefaultState(), i.tileentityData);
						}else if(i.blockState.getBlock() == Blocks.STONE_SLAB
								&& i.blockState.getValue(BlockStoneSlab.VARIANT) == EnumType.COBBLESTONE){
							return new Template.BlockInfo(i.pos, i.blockState.withProperty(BlockStoneSlab.VARIANT, EnumType.SMOOTHBRICK), i.tileentityData);
						}else if(i.blockState.getBlock() == Blocks.STONE_STAIRS){ // "Stone" stairs are actually cobblestone
							return new Template.BlockInfo(i.pos, BlockUtils.copyState(Blocks.STONE_BRICK_STAIRS, i.blockState), i.tileentityData);
						}
					}
					return i;
				},
				// Wood type
				new WoodTypeTemplateProcessor(woodType),
				// Mossifier
				new MossifierTemplateProcessor(mossiness, 0.04f, origin.getY() + 1),
				// Stone brick smasher-upper
				(w, p, i) -> i.blockState.getBlock() == Blocks.STONEBRICK && w.rand.nextFloat() < 0.1f ?
						new Template.BlockInfo(i.pos, Blocks.STONEBRICK.getDefaultState().withProperty(
								BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), i.tileentityData) : i,
				// Bookshelf marker
				(w, p, i) -> {
					TileEntityBookshelf.markAsNatural(i.tileentityData);
					return i;
				}
		);

		template.addBlocksToWorld(world, origin, processor, settings, 2 | 16);

		WizardryAntiqueAtlasIntegration.markLibrary(world, origin.getX(), origin.getZ());
	}

}
