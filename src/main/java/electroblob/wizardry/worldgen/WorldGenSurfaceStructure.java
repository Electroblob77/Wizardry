package electroblob.wizardry.worldgen;

import com.google.common.math.Quantiles;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Base class for all wizardry's above-ground structures, which handles various shared functions such as calculating
 * the median ground level and removing floating trees. This class implements
 * {@link WorldGenWizardryStructure#attemptPosition(Template, PlacementSettings, Random, World, int, int, String)}
 * but leaves the rest of the abstract methods to be implemented by subclasses.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
public abstract class WorldGenSurfaceStructure extends WorldGenWizardryStructure {

	/** The maximum fraction of the area where a structure is to be spawned that may be covered by liquid. */
	private static final float MAX_LIQUID_FRACTION = 0.4f;

	// This method calculates the median floor height to ensure that sudden changes in level are ignored and the
	// structure is always spawned at the same level as the majority of the underlying floor. Trees are also ignored
	// when determining floor level, so that forests don't impede structure spawning.
	@Override
	@Nullable
	protected BlockPos attemptPosition(Template template, PlacementSettings settings, Random random, World world,
									   int chunkX, int chunkZ, String structureFile){

		BlockPos size = template.transformedSize(settings.getRotation());

		// Offset by (8, 8) to minimise cascading worldgen lag, MINUS half the width of the structure (important!)
		// See https://www.reddit.com/r/feedthebeast/cowmments/5x0twz/investigating_extreme_worldgen_lag/?ref=share&ref_source=embed&utm_content=title&utm_medium=post_embed&utm_name=c07cbb545f74487793783012794733d8&utm_source=embedly&utm_term=5x0twz
		// Multiplying and left-shifting are identical but it's good practice to bitshift here I guess
		BlockPos origin = new BlockPos((chunkX << 4) + random.nextInt(16) + 8 - size.getX()/2, 0, (chunkZ << 4) + random.nextInt(16) + 8 - size.getZ()/2);

		// Estimate a starting height for searching for the floor
		BlockPos centre = world.getTopSolidOrLiquidBlock(new BlockPos(origin.add(size.getX()/2, 0, size.getZ()/2)));
		// Check if we're at the top of the world, and if so just randomise the y pos (accounts for 'cavern' dimensions)
		if(centre.getY() >= world.getActualHeight()) centre = new BlockPos(centre.getX(), random.nextInt(world.getActualHeight()), centre.getZ());

		Integer startingHeight = BlockUtils.getNearestSurface(world, centre, EnumFacing.UP, 32, true,
				BlockUtils.SurfaceCriteria.COLLIDABLE_IGNORING_TREES);

		if(startingHeight == null) return null;

		if(Wizardry.settings.fastWorldgen){
			BlockPos result = origin.up(startingHeight);
			// Fast worldgen only checks the central position for water, which isn't perfect but it is faster
			return world.getBlockState(new BlockPos(centre.getX(), startingHeight + 1, centre.getZ())).getMaterial().isLiquid() ? null : result;
		}

		int[] floorHeights = new int[size.getX() * size.getZ()];

		int liquidCount = 0;

		for(int i = 0; i < floorHeights.length; i++){
			// Despite what its name suggests, this method does not return the position of a liquid. It is in fact
			// exactly what is needed here since it is used for placing villages and stuff, and doesn't include leaves
			// or other foliage.
			BlockPos pos = origin.add(i / size.getZ(), 0, i % size.getZ());
			Integer floor = BlockUtils.getNearestSurface(world, pos.up(startingHeight), EnumFacing.UP, 32, true,
					BlockUtils.SurfaceCriteria.COLLIDABLE_IGNORING_TREES);
			floorHeights[i] = floor == null ? 0 : floor; // Very unlikely that floor is null
			// ^ That method gets the top solid block. Most non-solid blocks are ok to have around the structure,
			// with the exception of liquids, so if there are too many the position is deemed unsuitable.
			if(world.getBlockState(pos.up(floorHeights[i])).getMaterial().isLiquid()) liquidCount++;
			if(liquidCount > floorHeights.length * MAX_LIQUID_FRACTION) return null;
		}

		// Get the median floor height (rather than the mean, that way cliffs should have no effect)
		int medianFloorHeight = MathHelper.floor(Quantiles.median().compute(floorHeights));

		// Now we know the y level of the base of the structure, we can check for stuff in the way
		// A structure is deemed to have stuff in the way if the floor level at any of the (x, z) positions it
		// occupies differs from the base y level by more than its distance from the centre plus a constant.
		// In practical terms, this means structures can't spawn on steep slopes or inside cave mouths or buildings.

		for(int i = 0; i < floorHeights.length; i++){
			int orthogonalDist = Math.max(Math.abs(i / size.getZ() - size.getX()/2), Math.abs(i % size.getZ() - size.getZ()/2));
			if(Math.abs(floorHeights[i] - medianFloorHeight) > Math.max(2, orthogonalDist)) return null; // Something is in the way
		}

		return origin.up(medianFloorHeight - 1);
	}

	@Override
	protected void postGenerate(Random random, World world, PlacementSettings settings){
		if(!Wizardry.settings.fastWorldgen) removeFloatingTrees(world, settings.getBoundingBox(), random);
	}

	/** Finds and removes any floating bits of tree in and above the given structure bounding box. */
	protected static void removeFloatingTrees(World world, StructureBoundingBox boundingBox, Random random){

		boolean changed = true;
		int y = boundingBox.minY;

		// Remove all the logs

		while(changed && y < world.getHeight()){ // I do hope the trees don't reach the world height...

			// Always checks at least the first layer above the bounding box in case the structure cut the rest off
			if(y > boundingBox.maxY + 1) changed = false;

			for(int x = boundingBox.minX; x <= boundingBox.maxX; x++){
				for(int z = boundingBox.minZ; z <= boundingBox.maxZ; z++){

					BlockPos pos = new BlockPos(x, y, z);

					Block block = world.getBlockState(pos).getBlock();
					Block below = world.getBlockState(pos.down()).getBlock();

					if(block instanceof BlockLog){
						if(below != Blocks.GRASS && below != Blocks.DIRT && !BlockUtils.isTreeBlock(world, pos.down())){
							world.setBlockToAir(pos);
							changed = true;
						}
					}
				}
			}

			y++;
		}

		// Now update all leaves in the area 16 times to make them decay

		int border = 8;

		List<BlockPos> leaves = new ArrayList<>();

		for(int x = boundingBox.minX - border; x <= boundingBox.maxX + border; x++){
			for(int y1 = boundingBox.minY - border; y1 <= y + border; y1++){
				for(int z = boundingBox.minZ - border; z <= boundingBox.maxZ + border; z++){
					BlockPos pos = new BlockPos(x, y1, z);
					// Skip blocks that haven't been generated yet
					// If you think about it, there can't possibly be floating trees in unloaded chunks anyway
					if(!world.isBlockLoaded(pos)) continue;
					if(world.getBlockState(pos).getBlock() instanceof BlockLeaves) leaves.add(pos);
				}
			}
		}

		for(int i=0; i<16; i++){
			leaves.forEach(p -> world.getBlockState(p).getBlock().updateTick(world, p, world.getBlockState(p), random));
		}

		// Finally, remove all the items that were dropped as a result of leaf decay

		AxisAlignedBB box = new AxisAlignedBB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, y, boundingBox.maxZ).grow(border);

		world.getEntitiesWithinAABB(EntityItem.class, box).forEach(Entity::setDead);

	}

}
