package electroblob.wizardry.worldgen;

import net.minecraft.block.BlockStoneBrick;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;

import javax.annotation.Nullable;

/** Structure template processor that randomly 'mossifies' cobblestone and stone bricks in the structure. This is done
 * using weighting so there is more moss at the bottom, making it look more natural. */
// Behold, the ACME Mossifier 3000! (Patent pending)
public class MossifierTemplateProcessor implements ITemplateProcessor {

	private final float mossiness;
	private final float heightWeight;
	private final int groundLevel;

	/**
	 * Creates a new {@code MossifierTemplateProcessor} with the given parameters.
	 * @param mossiness The chance for each block in a given layer to be mossified.
	 * @param heightWeight The amount by which mossiness reduces for each subsequent level upwards.
	 * @param groundLevel The ground level for the structure, at which height is taken to be zero for the purposes of
	 *                    calculating mossiness.
	 */
	public MossifierTemplateProcessor(float mossiness, float heightWeight, int groundLevel){
		this.mossiness = mossiness;
		this.heightWeight = heightWeight;
		this.groundLevel = groundLevel;
	}

	@Nullable
	@Override
	public Template.BlockInfo processBlock(World world, BlockPos pos, Template.BlockInfo info){

		float chance = mossiness - heightWeight * (pos.getY() - groundLevel);

		if(world.rand.nextFloat() < chance){
			if(info.blockState.getBlock() == Blocks.COBBLESTONE){
				return new Template.BlockInfo(info.pos, Blocks.MOSSY_COBBLESTONE.getDefaultState(), info.tileentityData);
			}else if(info.blockState.getBlock() == Blocks.STONEBRICK){
				return new Template.BlockInfo(info.pos, Blocks.STONEBRICK.getDefaultState()
						.withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY), info.tileentityData);
			}
		}

		return info;
	}
}
