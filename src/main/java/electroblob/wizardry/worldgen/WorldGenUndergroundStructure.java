package electroblob.wizardry.worldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Base class for all wizardry's underground structures, which handles finding a location with a cave entrance (based
 * on {@code WorldGenDungeons}, with some tweaks). This class implements
 * {@link WorldGenWizardryStructure#attemptPosition(Template, PlacementSettings, Random, World, int, int, String)}
 * but leaves the rest of the abstract methods to be implemented by subclasses.
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public abstract class WorldGenUndergroundStructure extends WorldGenWizardryStructure {

	private static final int MAX_ENTRANCES = 5;

	@Nullable
	@Override
	protected BlockPos attemptPosition(Template template, PlacementSettings settings, Random random, World world, int chunkX, int chunkZ, String structureFile){

		BlockPos origin = new BlockPos(8 + (chunkX << 4) + random.nextInt(16), 20 + random.nextInt(40), 8 + (chunkZ << 4) + random.nextInt(16));

		BlockPos size = template.transformedSize(settings.getRotation());
		BlockPos corner = origin.add(size.getX(), 1, size.getZ()); // Need not iterate through everything

		// Criteria for a valid position:
		// - There must be at least one entrance at floor level that a player can fit through, and no more than 5
		// - All blocks that the floor will replace must be solid
		// - Unlike with dungeons, there CAN be holes in the ceiling (mainly because it's not flat!)

		int entrances = 0;

		for(BlockPos pos : BlockPos.getAllInBox(origin, corner)){

			// Assume the bottom layer of the structure to be the floor
			if(pos.getY() == origin.getY() && !world.getBlockState(pos).getMaterial().isSolid()){
				return null; // Can't generate with space underneath
			}

			if((pos.getX() == origin.getX() || pos.getX() == corner.getX() || pos.getZ() == origin.getZ() || pos.getZ() == corner.getZ())
					&& pos.getY() == origin.getY() + 1 && world.isAirBlock(pos) && world.isAirBlock(pos.up())){
				if(entrances++ > MAX_ENTRANCES) return null;
			}
		}

		if(entrances == 0) return null; // No way in!

		return origin;
	}

}
