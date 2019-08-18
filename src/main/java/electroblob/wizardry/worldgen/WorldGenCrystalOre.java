package electroblob.wizardry.worldgen;

import com.google.common.primitives.Ints;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WorldGenCrystalOre implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider){

		if(Ints.contains(Wizardry.settings.oreDimensions, world.provider.getDimension())){
			this.addOreSpawn(WizardryBlocks.crystal_ore.getDefaultState(), world, random, chunkX * 16, chunkZ * 16, 16, 16, 5, 7, 5, 30);
		}
	}

	/**
	 * Adds an Ore Spawn to Minecraft. Simply register all Ores to spawn with this method in your Generation method in
	 * your IWorldGeneration extending Class
	 *
	 * @param state The Block to spawn
	 * @param world The World to spawn in
	 * @param random A Random object for retrieving random positions within the world to spawn the Block
	 * @param blockXPos An int for passing the X-Coordinate for the Generation method
	 * @param blockZPos An int for passing the Z-Coordinate for the Generation method
	 * @param maxX An int for setting the maximum X-Coordinate values for spawning on the X-Axis on a Per-Chunk basis
	 * @param maxZ An int for setting the maximum Z-Coordinate values for spawning on the Z-Axis on a Per-Chunk basis
	 * @param maxVeinSize An int for setting the maximum size of a vein
	 * @param chancesToSpawn An int for the Number of chances available for the Block to spawn per-chunk
	 * @param minY An int for the minimum Y-Coordinate height at which this block may spawn
	 * @param maxY An int for the maximum Y-Coordinate height at which this block may spawn
	 **/
	public void addOreSpawn(IBlockState state, World world, Random random, int blockXPos, int blockZPos, int maxX,
							int maxZ, int maxVeinSize, int chancesToSpawn, int minY, int maxY){
		// int maxPossY = minY + (maxY - 1);
		assert maxY > minY : "The maximum Y must be greater than the Minimum Y";
		assert maxX > 0 && maxX <= 16 : "addOreSpawn: The Maximum X must be greater than 0 and less than 16";
		assert minY > 0 : "addOreSpawn: The Minimum Y must be greater than 0";
		assert maxY < 256 && maxY > 0 : "addOreSpawn: The Maximum Y must be less than 256 but greater than 0";
		assert maxZ > 0 && maxZ <= 16 : "addOreSpawn: The Maximum Z must be greater than 0 and less than 16";

		int diffBtwnMinMaxY = maxY - minY;
		for(int x = 0; x < chancesToSpawn; x++){
			int posX = blockXPos + random.nextInt(maxX);
			int posY = minY + random.nextInt(diffBtwnMinMaxY);
			int posZ = blockZPos + random.nextInt(maxZ);
			// N.B. This method applies the anti-cascading-lag offset itself
			(new WorldGenMinable(state, maxVeinSize)).generate(world, random, new BlockPos(posX, posY, posZ));
		}
	}

}
