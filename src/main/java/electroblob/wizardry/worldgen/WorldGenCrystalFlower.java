package electroblob.wizardry.worldgen;

import com.google.common.primitives.Ints;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WorldGenCrystalFlower implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider){

		if(Ints.contains(Wizardry.settings.flowerDimensions, world.provider.getDimension())){
			this.generatePlant(WizardryBlocks.crystal_flower.getDefaultState(), world, random, 8 + chunkX * 16, 8 + chunkZ * 16, 2, 20);
		}
	}

	/**
	 * Generates the specified plant randomly throughout the world.
	 *
	 * @param state The plant block
	 * @param world The world
	 * @param random A instance of {@code Random} to use
	 * @param x The x coordinate of the first block in the chunk
	 * @param z The y coordinate of the first block in the chunk
	 * @param chancesToSpawn Number of chances to spawn a flower patch
	 * @param groupSize The number of times to try generating a flower per flower patch spawn
	 */
	public void generatePlant(IBlockState state, World world, Random random, int x, int z, int chancesToSpawn, int groupSize){

		for(int i = 0; i < chancesToSpawn; i++){

			int randPosX = x + random.nextInt(16);
			int randPosY = random.nextInt(256);
			int randPosZ = z + random.nextInt(16);

			for(int l = 0; l < groupSize; ++l){

				int i1 = randPosX + random.nextInt(8) - random.nextInt(8);
				int j1 = randPosY + random.nextInt(4) - random.nextInt(4);
				int k1 = randPosZ + random.nextInt(8) - random.nextInt(8);

				BlockPos pos = new BlockPos(i1, j1, k1);

				if(world.isBlockLoaded(pos) && world.isAirBlock(pos) && (!world.provider.isNether() || j1 < 127)
						&& state.getBlock().canPlaceBlockOnSide(world, pos, EnumFacing.UP)){

					world.setBlockState(pos, state, 2);
				}
			}
		}
	}
}
