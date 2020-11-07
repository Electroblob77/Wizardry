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

		BlockPos size = template.transformedSize(settings.getRotation());

		// Offset by (8, 8) to minimise cascading worldgen lag, MINUS half the width of the structure (important!)
		// See https://www.reddit.com/r/feedthebeast/cowmments/5x0twz/investigating_extreme_worldgen_lag/?ref=share&ref_source=embed&utm_content=title&utm_medium=post_embed&utm_name=c07cbb545f74487793783012794733d8&utm_source=embedly&utm_term=5x0twz
		// Multiplying and left-shifting are identical but it's good practice to bitshift here I guess
		BlockPos origin = new BlockPos((chunkX << 4) + random.nextInt(16) + 8 - size.getX()/2, 20 + random.nextInt(40), (chunkZ << 4) + random.nextInt(16) + 8 - size.getZ()/2);

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
