package electroblob.wizardry.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockPermafrost extends Block {

	public BlockPermafrost(){
		super(Material.ICE);
	}

	@Override
	public float getSlipperiness(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity entity){
		return super.getSlipperiness(state, world, pos, entity);
	}
}
