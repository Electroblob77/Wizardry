package electroblob.wizardry.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

/** Like {@link net.minecraft.block.BlockFrostedIce}, but for lava instead of water. */
// This is mostly copied from that class, with a few changes
public class BlockObsidianCrust extends BlockObsidian {

	public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

	public BlockObsidianCrust(){
		this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(AGE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(AGE, MathHelper.clamp(meta, 0, 3));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random){
		if((random.nextInt(3) == 0 || this.countNeighbors(world, pos) < 4) && world.getLightFromNeighbors(pos) > 11 - state.getValue(AGE) - state.getLightOpacity()){
			this.slightlyMelt(world, pos, state, random, true);
		}else{
			world.scheduleUpdate(pos, this, MathHelper.getInt(random, 20, 40));
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos){
		if(block == this){
			int i = this.countNeighbors(world, pos);

			if(i < 2){
				this.melt(world, pos);
			}
		}
	}

	private int countNeighbors(World world, BlockPos pos){

		int i = 0;

		for(EnumFacing enumfacing : EnumFacing.values()){
			if(world.getBlockState(pos.offset(enumfacing)).getBlock() == this){
				++i;

				if(i >= 4){
					return i;
				}
			}
		}

		return i;
	}

	protected void slightlyMelt(World world, BlockPos pos, IBlockState state, Random random, boolean meltNeighbours){

		int i = state.getValue(AGE);

		if(i < 3){

			world.setBlockState(pos, state.withProperty(AGE, i + 1), 2);
			world.scheduleUpdate(pos, this, MathHelper.getInt(random, 20, 40));

		}else{

			this.melt(world, pos);

			if(meltNeighbours){

				for(EnumFacing enumfacing : EnumFacing.values()){

					BlockPos blockpos = pos.offset(enumfacing);
					IBlockState iblockstate = world.getBlockState(blockpos);

					if(iblockstate.getBlock() == this){
						this.slightlyMelt(world, blockpos, iblockstate, random, false);
					}
				}
			}
		}
	}

	protected void melt(World world, BlockPos pos){
		world.setBlockState(pos, Blocks.LAVA.getDefaultState());
		world.neighborChanged(pos, Blocks.LAVA, pos);
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, AGE);
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state){
		return ItemStack.EMPTY;
	}
}
