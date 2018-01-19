package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// For future reference - extend BlockContainer whenever possible because it has methods for removing tile entities on block break.
public class BlockVanishingCobweb extends BlockContainer {

	public BlockVanishingCobweb(Material material) {
		super(material);
	}
	
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

	@Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }
	
	@Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
    
    @Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityTimer(400);
	}
    
    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity){
        entity.setInWeb();
    }
 
    @Override
	public int quantityDropped(Random par1Random){
        return 0;
    }
	
}
