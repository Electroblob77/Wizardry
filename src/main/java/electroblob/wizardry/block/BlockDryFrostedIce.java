package electroblob.wizardry.block;

import net.minecraft.block.BlockFrostedIce;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

/** Like {@link BlockFrostedIce}, but melting does not depend on light level or neighbouring blocks, and it just
 * disappears instead of turning to water. */
public class BlockDryFrostedIce extends BlockFrostedIce {

	@Override
	protected void turnIntoWater(World world, BlockPos pos){
		world.destroyBlock(pos, false);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand){
		if(rand.nextInt(3) == 0){
			this.slightlyMelt(worldIn, pos, state, rand, true);
		}else{
			worldIn.scheduleUpdate(pos, this, MathHelper.getInt(rand, 20, 40));
		}
	}
}
