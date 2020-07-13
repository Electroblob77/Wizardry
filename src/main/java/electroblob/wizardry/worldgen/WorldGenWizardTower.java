package electroblob.wizardry.worldgen;

import com.google.common.collect.ImmutableMap;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.integration.antiqueatlas.WizardryAntiqueAtlasIntegration;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.GeometryUtils;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WorldGenWizardTower extends WorldGenSurfaceStructure {

	// TODO: Add wizard towers to the /locate command
	// This requires some careful manipulation of Random objects to replicate the positions exactly for the current
	// world. See the end of ChunkGeneratorOverworld for the relevant methods.

	private static final String WIZARD_DATA_BLOCK_TAG = "wizard";
	private static final String EVIL_WIZARD_DATA_BLOCK_TAG = "evil_wizard";

	private final Map<BiomeDictionary.Type, IBlockState> specialWallBlocks;

	public WorldGenWizardTower(){
		// These are initialised here because it's a convenient point after the blocks are registered
		specialWallBlocks = ImmutableMap.of(
				BiomeDictionary.Type.MESA, Blocks.RED_SANDSTONE.getDefaultState(),
				BiomeDictionary.Type.MOUNTAIN, Blocks.STONEBRICK.getDefaultState(),
				BiomeDictionary.Type.NETHER, Blocks.NETHER_BRICK.getDefaultState(),
				BiomeDictionary.Type.SANDY, Blocks.SANDSTONE.getDefaultState()
		);
	}

	@Override
	public String getStructureName(){
		return "wizard_tower";
	}

	@Override
	public long getRandomSeedModifier(){
		return 10473957L; // Yep, I literally typed 8 digits at random
	}

	@Override
	public boolean canGenerate(Random random, World world, int chunkX, int chunkZ){
		return ArrayUtils.contains(Wizardry.settings.towerDimensions, world.provider.getDimension())
				&& Wizardry.settings.towerRarity > 0 && random.nextInt(Wizardry.settings.towerRarity) == 0;
	}

	@Override
	public ResourceLocation getStructureFile(Random random){
		return random.nextDouble() < Wizardry.settings.evilWizardChance ?
				Wizardry.settings.towerWithChestFiles[random.nextInt(Wizardry.settings.towerWithChestFiles.length)] :
				Wizardry.settings.towerFiles[random.nextInt(Wizardry.settings.towerFiles.length)];
	}

	@Override
	public void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile){

		final EnumDyeColor colour = EnumDyeColor.values()[random.nextInt(EnumDyeColor.values().length)];
		final Biome biome = world.getBiome(origin);

		final IBlockState wallMaterial = specialWallBlocks.keySet().stream().filter(t -> BiomeDictionary.hasType(biome, t))
				.findFirst().map(specialWallBlocks::get).orElse(Blocks.COBBLESTONE.getDefaultState());

		final float mossiness = getBiomeMossiness(biome);
		final BlockPlanks.EnumType woodType = BlockUtils.getBiomeWoodVariant(biome);

		final Set<BlockPos> blocksPlaced = new HashSet<>();

		ITemplateProcessor processor = new MultiTemplateProcessor(true,
				// Roof colour
				(w, p, i) -> i.blockState.getBlock() instanceof BlockStainedHardenedClay ? new Template.BlockInfo(
						i.pos, i.blockState.withProperty(BlockStainedHardenedClay.COLOR, colour), i.tileentityData) : i,
				// Wall material
				(w, p, i) -> i.blockState.getBlock() == Blocks.COBBLESTONE ? new Template.BlockInfo(i.pos,
						wallMaterial, i.tileentityData) : i,
				// Wood type
				new WoodTypeTemplateProcessor(woodType),
				// Mossifier
				new MossifierTemplateProcessor(mossiness, 0.04f, origin.getY() + 1),
				// Block recording (the process() method doesn't get called for structure voids)
				(w, p, i) -> {if(i.blockState.getBlock() != Blocks.AIR) blocksPlaced.add(p); return i;}
		);

		template.addBlocksToWorld(world, origin, processor, settings, 2 | 16);

		WizardryAntiqueAtlasIntegration.markTower(world, origin.getX(), origin.getZ());

		// Wizard spawning
		Map<BlockPos, String> dataBlocks = template.getDataBlocks(origin, settings);

		for(Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()){

			Vec3d vec = GeometryUtils.getCentre(entry.getKey());

			if(entry.getValue().equals(WIZARD_DATA_BLOCK_TAG)){

				EntityWizard wizard = new EntityWizard(world);
				wizard.setLocationAndAngles(vec.x, vec.y, vec.z, 0, 0);
				wizard.onInitialSpawn(world.getDifficultyForLocation(origin), null);
				wizard.setTowerBlocks(blocksPlaced);
				world.spawnEntity(wizard);

			}else if(entry.getValue().equals(EVIL_WIZARD_DATA_BLOCK_TAG)){

				EntityEvilWizard wizard = new EntityEvilWizard(world);
				wizard.setLocationAndAngles(vec.x, vec.y, vec.z, 0, 0);
				wizard.hasStructure = true; // Stops it despawning
				wizard.onInitialSpawn(world.getDifficultyForLocation(origin), null);
				world.spawnEntity(wizard);

			}else{
				// This probably shouldn't happen...
				Wizardry.logger.info("Unrecognised data block value {} in structure {}", entry.getValue(), structureFile);
			}
		}
	}

	private static float getBiomeMossiness(Biome biome){
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.DENSE)) 		return 0.7f;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) 	return 0.7f;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET)) 		return 0.5f;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) 		return 0.5f;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST)) 	return 0.3f;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.LUSH)) 		return 0.3f;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.DRY)) 		return 0;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)) 		return 0;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.DEAD)) 		return 0;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.WASTELAND)) 	return 0;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)) 	return 0;
		return 0.1f; // Everything else (plains, etc.) has a small amount of moss
	}

}
